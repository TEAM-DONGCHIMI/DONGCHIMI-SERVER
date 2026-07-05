# Config & Auth — 설정 바인딩 & 인증 컨벤션

---

## 1. 모듈별 yml 관리

각 모듈은 자신의 설정값을 `src/main/resources/application-{모듈명}.yml`에 정의한다.  
`bootstrap` 모듈의 `application.yml`에서 `spring.config.import`로 해당 파일을 가져온다.

```
bootstrap/src/main/resources/application.yml
gateway/auth/src/main/resources/application-gateway-auth.yml
api/core-api/src/main/resources/application-core-api.yml
```

```yaml
# bootstrap/application.yml
spring:
  config:
    import:
      - classpath:application-gateway-auth.yml
      - classpath:application-core-api.yml
```

> `bootstrap` 모듈에 설정을 직접 작성하지 않는다. 설정의 소유권은 해당 모듈에 있다.

> 각 모듈의 yml이 classpath에 올라오려면 `bootstrap`이 해당 모듈을 `implementation`으로 **직접** 의존해야 한다. `runtimeOnly`나 전이 의존성만으로는 IntelliJ에서 `processResources`가 실행되지 않아 파일을 찾지 못한다.

---

## 2. 환경변수 관리

yml에서 `${ENV_VAR}` 형태로 환경변수를 참조할 때는 반드시 루트의 `.env.example`에도 항목을 추가한다.

```yaml
# application-gateway-auth.yml 예시
jwt:
  secret-key: ${JWT_SECRET_KEY}
  access-token-expiry: ${JWT_ACCESS_TOKEN_EXPIRY:3600000}  # 기본값이 있으면 함께 기재
```

```dotenv
# .env.example — 위 yml과 항상 동기화
JWT_SECRET_KEY=your-secret-key-here
JWT_ACCESS_TOKEN_EXPIRY=3600000
```

- 기본값(`${VAR:default}`)이 있는 경우 `.env.example`에도 동일한 값을 예시로 기재한다.
- 필수값(`${VAR}`)은 실제 값 대신 의도를 나타내는 플레이스홀더를 작성한다. (예: `your-secret-key-here`)
- `.env.example`은 `.gitignore`에 포함하지 않는다. 실제 값이 담긴 `.env`는 커밋하지 않는다.

---

## 3. 설정 바인딩

- `@ConfigurationProperties`를 사용한다. `@Value`는 사용하지 않는다.
- `data class`로 선언한다.

```kotlin
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secretKey: String,
    val issuer: String,
    val accessTokenExpiry: Long,
)
```

---

## 4. 인증 흐름

role별 인가는 두 단계로 이뤄진다.

1. **`SecurityConfig`의 URL 패턴 인가** — role별 API는 각각 고정 경로 prefix(`/v1/owners/**`, `/v1/admin/**`, `/v1/users/**`)를 가지며, `hasAuthority(Role.X.name)`으로 매칭한다. 인증 여부만 확인하는 게 아니라 role 자체를 여기서 1차로 걸러낸다.
2. **role별 `ApiUser` Argument Resolver** — role별 Controller는 자신의 role 전용 `ApiUser` 구현체(`OwnerApiUser`/`AdminApiUser`/`UserApiUser`)만 파라미터로 받는다. Resolver가 `PrincipalProvider.roles`에 해당 role이 없으면 `CoreException(CommonErrorCode.FORBIDDEN)`(403)을 던진다.

```
HTTP Request (Authorization: Bearer <token>)
    ↓
JwtAuthFilter                   — 토큰 검증 → SecurityContextHolder에 UserAuthentication 저장
    ↓
SecurityConfig                  — URL 패턴별 hasAuthority(Role)로 1차 인가
    ↓
{Role}ApiUserArgumentResolver   — PrincipalProvider.roles에 해당 role이 없으면 CoreException(CommonErrorCode.FORBIDDEN), 있으면 {Role}ApiUser 스냅샷 생성 후 파라미터 주입
    ↓
Controller (apiUser: OwnerApiUser / AdminApiUser / UserApiUser)
```

> `authorizeHttpRequests`의 규칙은 선언 순서대로 매칭되므로, role별 패턴은 반드시 `anyRequest`보다 먼저 선언해야 한다. `anyRequest`가 먼저 오면 뒤에 오는 role별 규칙은 절대 도달하지 않는다.
>
> 권한 문자열은 `Role.X.name`(예: `"OWNER"`)을 그대로 쓴다. `JwtProvider`가 발급한 토큰의 `roles` claim과 `UserAuthentication`이 `SimpleGrantedAuthority(role)`로 저장하는 값 모두 `"ROLE_"` 접두사가 없으므로, `hasRole()`이 아니라 `hasAuthority()`를 사용한다 (`hasRole()`은 내부적으로 `"ROLE_"`을 자동으로 붙인다).

`api:core-api`의 `WebMvcConfig`는 특정 role 모듈을 의존하지 않고, 컨텍스트에 등록된 `HandlerMethodArgumentResolver` 빈을 전부 주입받아 등록한다. `bootstrap`이 `api:owner-api`/`api:admin-api`/`api:user-api`를 모두 의존하고 `scanBasePackages = ["kr.dongchimi"]`로 전체를 스캔하므로 role별 Resolver가 자동으로 수집된다.

### 모듈별 역할

| 모듈 | 파일 | 역할 |
| --- | --- | --- |
| `core` | `auth/PrincipalProvider.kt` | 인터페이스 (`userId`, `roles`) |
| `core` | `auth/Role.kt` | role enum (`USER`, `OWNER`, `ADMIN`) |
| `gateway:auth` | `security/SecurityPrincipalProvider.kt` | `SecurityContextHolder` 기반 구현체 |
| `gateway:auth` | `security/UserAuthentication.kt` | `UsernamePasswordAuthenticationToken` 래퍼 |
| `gateway:auth` | `jwt/JwtProvider.kt` | JWT 생성·파싱 |
| `gateway:auth` | `jwt/JwtAuthFilter.kt` | 토큰 검증 후 SecurityContext 저장 |
| `gateway:auth` | `config/SecurityConfig.kt` | FilterChain, URL 패턴별 role 인가 규칙 |
| `gateway:auth` | `config/JwtProperties.kt` | JWT 설정 바인딩 |
| `gateway:auth` | `config/CorsProperties.kt` | CORS 설정 바인딩 |
| `api:core-api` | `core/ApiUser.kt` | 인증 사용자 스냅샷 인터페이스 (`userId`, `roles`) |
| `api:core-api` | `core/config/WebMvcConfig.kt` | 컨텍스트의 모든 `HandlerMethodArgumentResolver` 빈 등록 |
| `api:owner-api` | `owner/OwnerApiUser.kt`, `owner/resolver/OwnerApiUserArgumentResolver.kt` | OWNER 전용 `ApiUser` 구현체 + 파라미터 주입 |
| `api:admin-api` | `admin/AdminApiUser.kt`, `admin/resolver/AdminApiUserArgumentResolver.kt` | ADMIN 전용 `ApiUser` 구현체 + 파라미터 주입 |
| `api:user-api` | `user/UserApiUser.kt`, `user/resolver/UserApiUserArgumentResolver.kt` | USER 전용 `ApiUser` 구현체 + 파라미터 주입 |

---

## 5. 사용 규칙

### Controller

어노테이션 없이 자신이 속한 role 모듈의 `ApiUser` 구현체로 선언하면 자동 주입된다 (예: `api:owner-api`의 Controller는 `OwnerApiUser`).

```kotlin
// api:owner-api
@PostMapping
fun createPost(
    apiUser: OwnerApiUser,
    @Valid @RequestBody request: CreatePostRequest,
): CommonResponse<Void>
```

### Swagger 인터페이스(`*Api`)

`@Parameter(hidden = true)`를 반드시 붙인다.

```kotlin
fun createPost(
    @Parameter(hidden = true) apiUser: OwnerApiUser,
    @RequestBody request: CreatePostRequest,
): CommonResponse<Void>
```

### Service

`principal.userId`를 꺼내서 `Long`으로 전달한다. Service는 `ApiUser`에 의존하지 않는다.

```kotlin
postService.createPost(principal.userId, request.toCommand())
```

### 인증 불필요 API

`SecurityConfig`에 `permitAll` 경로를 추가하고, Controller에서 `ApiUser` 파라미터를 선언하지 않는다.

```kotlin
// SecurityConfig
authorizeHttpRequests {
    authorize("/auth/login", permitAll)
    authorize(anyRequest, authenticated)
}
```

### 테스트 환경

`test` 프로파일에서 `PrincipalProvider` 스텁을 `@Primary`로 등록한다.

```kotlin
@Profile("test")
@Primary
@Component
class StubPrincipalProvider : PrincipalProvider {
    override val userId: Long = 1L
    override val roles: Set<String> = setOf("USER")
}
```
