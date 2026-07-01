# Config & Auth — 설정 바인딩 & 인증 컨벤션

---

## 1. 모듈별 yml 관리

각 모듈은 자신의 설정값을 `src/main/resources/application-{모듈명}.yml`에 정의한다.  
`bootstrap` 모듈의 `application.yml`에서 `spring.config.import`로 해당 파일을 가져온다.

```
bootstrap/src/main/resources/application.yml
gateway/auth/src/main/resources/application-gateway-auth.yml
api/src/main/resources/application-api.yml
```

```yaml
# bootstrap/application.yml
spring:
  config:
    import:
      - classpath:application-gateway-auth.yml
      - classpath:application-api.yml
```

> `bootstrap` 모듈에 설정을 직접 작성하지 않는다. 설정의 소유권은 해당 모듈에 있다.

> 각 모듈의 yml이 classpath에 올라오려면 `bootstrap`이 해당 모듈을 `implementation`으로 **직접** 의존해야 한다. `runtimeOnly`나 전이 의존성만으로는 IntelliJ에서 `processResources`가 실행되지 않아 파일을 찾지 못한다.

---

## 2. 환경변수 관리

yml에서 `${ENV_VAR}` 형태로 환경변수를 참조할 때는 반드시 루트의 `.env.example`에도 항목을 추가한다.

```yaml
# application-gateway:auth.yml 예시
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

```
HTTP Request (Authorization: Bearer <token>)
    ↓
JwtAuthFilter              — 토큰 검증 → SecurityContextHolder에 UserAuthentication 저장
    ↓
ApiUserArgumentResolver    — PrincipalProvider 빈으로 ApiUser 스냅샷 생성 후 파라미터 주입
    ↓
Controller (principal: ApiUser)
```

### 모듈별 역할

| 모듈 | 파일 | 역할 |
| --- | --- | --- |
| `core` | `auth/PrincipalProvider.kt` | 인터페이스 (`userId`, `roles`) |
| `gateway:auth` | `security/SecurityPrincipalProvider.kt` | `SecurityContextHolder` 기반 구현체 |
| `gateway:auth` | `security/UserAuthentication.kt` | `UsernamePasswordAuthenticationToken` 래퍼 |
| `gateway:auth` | `jwt/JwtProvider.kt` | JWT 생성·파싱 |
| `gateway:auth` | `jwt/JwtAuthFilter.kt` | 토큰 검증 후 SecurityContext 저장 |
| `gateway:auth` | `config/SecurityConfig.kt` | FilterChain, 인가 규칙 |
| `gateway:auth` | `config/JwtProperties.kt` | JWT 설정 바인딩 |
| `gateway:auth` | `config/CorsProperties.kt` | CORS 설정 바인딩 |
| `api` | `common/ApiUser.kt` | 인증 사용자 스냅샷 DTO (`userId`, `roles`) |
| `api` | `common/resolver/ApiUserArgumentResolver.kt` | `ApiUser` 파라미터 주입 |
| `api` | `common/config/WebMvcConfig.kt` | ArgumentResolver 등록 |

---

## 5. 사용 규칙

### Controller

어노테이션 없이 `ApiUser`로 선언하면 자동 주입된다.

```kotlin
@PostMapping
fun createPost(
    apiUser: ApiUser,
    @Valid @RequestBody request: CreatePostRequest,
): CommonResponse<Void>
```

### Swagger 인터페이스(`*Api`)

`@Parameter(hidden = true)`를 반드시 붙인다.

```kotlin
fun createPost(
    @Parameter(hidden = true) apiUser: ApiUser,
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
