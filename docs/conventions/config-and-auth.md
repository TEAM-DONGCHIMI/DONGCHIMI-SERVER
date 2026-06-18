# Config & Auth — 설정 바인딩 & PrincipalProvider 인증 컨벤션

> 이 문서는 `@ConfigurationProperties` 설정 바인딩 규칙과, JWT 기반 인증에서 사용하는
> `PrincipalProvider`의 동작 흐름·사용법을 다룬다.
> 설정값 추가, 인증이 필요한 API 작업 시 참조한다.
>
> **모듈 위치**: `PrincipalProvider` 인터페이스는 `core`에 정의하고, 구현체는 `gateway-auth`에 둔다.
> `api`는 `gateway-auth`를 `runtimeOnly`로만 선언해 컴파일 타임 의존을 차단한다.

---

## 1. 설정 바인딩

- `@ConfigurationProperties`를 사용하며 `@Value`는 사용하지 않는다
- `data class`로 선언한다

```kotlin
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secretKey: String,
    val issuer: String,
    val accessTokenExpiry: Long,
)
```

---

## 2. PrincipalProvider 인증 컨벤션

### 2-1. 개요

`PrincipalProvider`는 `core`에 정의된 인터페이스로, 인증된 사용자 정보를 Controller 메서드 파라미터로 주입받기 위해 사용한다.
`api`는 인터페이스만 참조하고(`core` 의존), 실제 구현체(`SecurityPrincipalProvider`)는 `gateway-auth`에 위치한다.
`api`가 `gateway-auth`를 `runtimeOnly`로 선언하므로, 컴파일 타임에 구현체에 직접 의존하지 않는다.

### 2-2. 모듈별 역할

| 모듈 | 파일 | 역할 |
| --- | --- | --- |
| `core` | `auth/PrincipalProvider.kt` | 인터페이스 정의 (`userId`, `roles`, `email`) |
| `gateway-auth` | `auth/SecurityPrincipalProvider.kt` | `SecurityContextHolder` 기반 구현체 |
| `gateway-auth` | `security/UserAuthentication.kt` | `UsernamePasswordAuthenticationToken` 래퍼 |
| `gateway-auth` | `security/jwt/JwtAuthFilter.kt` | 토큰 검증 후 SecurityContext에 저장 |
| `gateway-auth` | `security/config/SecurityConfig.kt` | Security FilterChain, 경로 인가 규칙 설정 |
| `api` | `common/resolver/PrincipalProviderArgumentResolver.kt` | `PrincipalProvider` 빈을 파라미터에 주입 |
| `api` | `common/config/WebMvcConfig.kt` | ArgumentResolver 등록 |

### 2-3. 동작 흐름

```
HTTP Request (Authorization: Bearer <token>)
    ↓
JwtAuthFilter (gateway-auth)
    - 토큰 추출 및 검증
    - SecurityContextHolder에 UserAuthentication 저장
    ↓
PrincipalProviderArgumentResolver (api)
    - PrincipalProvider 빈(= SecurityPrincipalProvider)을 파라미터에 주입
    ↓
Controller 메서드 (principal: PrincipalProvider)
    - principal.userId / principal.email 프로퍼티 접근
    → SecurityContextHolder에서 매번 최신 값을 읽음
```

### 2-4. 사용법

**1. Controller — 파라미터로 선언**

어노테이션 없이 `PrincipalProvider` 타입으로 선언하면 자동 주입된다.

```kotlin
@PostMapping
fun createPost(
    principal: PrincipalProvider,
    @Valid @RequestBody request: CreatePostRequest,
): CommonResponse<Void> {
    postService.createPost(principal.userId, request.toCommand())
    return CommonResponse.success(PostSuccessCode.POST_CREATED)
}
```

**2. Swagger 인터페이스(`*Api`) — `@Parameter(hidden = true)` 추가**

Swagger UI에 노출되지 않도록 반드시 숨김 처리한다.

```kotlin
@Operation(summary = "게시글 작성", description = "새로운 게시글을 생성합니다.")
fun createPost(
    @Parameter(hidden = true) principal: PrincipalProvider,
    @RequestBody request: CreatePostRequest,
): CommonResponse<Void>
```

**3. Service — `userId`를 `Long`으로 직접 받는다**

Controller에서 `principal.userId`를 추출한 후 Service에 전달한다. Service는 `PrincipalProvider`에 의존하지 않는다.

```kotlin
// Controller
postService.createPost(principal.userId, request.toCommand())

// Service
fun createPost(userId: Long, command: CreatePostCommand) { ... }
```

### 2-5. 규칙 요약

| 위치 | 규칙 |
| --- | --- |
| Controller | `principal: PrincipalProvider` 파라미터 선언, 어노테이션 없음 |
| `*Api` 인터페이스 | `@Parameter(hidden = true)` 필수 |
| Service | `Long userId`를 받음 (`PrincipalProvider` 직접 의존 X) |

### 2-6. 인증이 필요 없는 API

`SecurityConfig`의 `authorizeHttpRequests`에서 permit된 경로는 토큰 없이 호출된다.
이 경우 `SecurityContextHolder`에 인증 정보가 없으므로 **`PrincipalProvider` 파라미터를 선언하지 않는다.**

```kotlin
// 인증 불필요 — PrincipalProvider 파라미터 없음
@PostMapping("/login")
fun login(@RequestBody request: LoginRequest, response: HttpServletResponse): CommonResponse<LoginResponse> { ... }

// 인증 필요 — PrincipalProvider 파라미터 있음
@PostMapping("/logout")
fun logout(principal: PrincipalProvider, httpRequest: HttpServletRequest): CommonResponse<Void> { ... }
```

### 2-7. 테스트 환경 대응

`test` 프로파일에서 `SecurityPrincipalProvider` 대신 고정값을 반환하는 스텁을 등록한다.

```kotlin
@Profile("test")
@Primary
@Component
class StubPrincipalProvider : PrincipalProvider {
    override val userId: Long = 1L
    override val roles: Set<String> = setOf("USER")
    override val email: String = "test@example.com"
}
```
