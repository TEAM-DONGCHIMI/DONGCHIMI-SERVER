# Config & Auth — 설정 바인딩 & PrincipalProvider 인증 컨벤션

> 이 문서는 `@ConfigurationProperties` 설정 바인딩 규칙과, JWT 기반 인증에서 사용하는
> `PrincipalProvider`의 동작 흐름·사용법을 다룬다.
> 설정값 추가, 인증이 필요한 API 작업 시 참조한다.

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

`PrincipalProvider`는 JWT Access Token에서 추출한 `userId`를 Controller 메서드 파라미터로 주입받기 위한 객체이다.
Spring의 `HandlerMethodArgumentResolver`를 통해 자동으로 주입되므로, 어노테이션 없이 파라미터 타입만으로 동작한다.

### 2-2. 동작 흐름

```
HTTP Request (Authorization: Bearer <token>)
    ↓
JwtAuthFilter
    - 토큰 추출 및 검증
    - userId 파싱 → PrincipalProvider.of(userId) 생성
    - SecurityContextHolder에 UserAuthentication 저장
    ↓
PrincipalProviderArgumentResolver
    - SecurityContextHolder에서 PrincipalProvider를 꺼내서 파라미터에 주입
    ↓
Controller 메서드 (provider: PrincipalProvider)
```

### 2-3. 사용법

**1. Controller — 파라미터로 선언**

어노테이션 없이 `PrincipalProvider` 타입으로 선언하면 자동 주입된다.

```kotlin
@PostMapping
fun createPost(
    provider: PrincipalProvider,
    @Valid @RequestBody request: CreatePostRequest,
): CommonResponse<Void> {
    postService.createPost(provider.userId(), request.toCommand())
    return CommonResponse.success(PostSuccessCode.POST_CREATED)
}
```

**2. Swagger 인터페이스(`*Api`) — `@Parameter(hidden = true)` 추가**

Swagger UI에 노출되지 않도록 반드시 숨김 처리한다.

```kotlin
@Operation(summary = "게시글 작성", description = "새로운 게시글을 생성합니다.")
fun createPost(
    @Parameter(hidden = true) provider: PrincipalProvider,
    @RequestBody request: CreatePostRequest,
): CommonResponse<Void>
```

**3. Service — `userId`를 `Long`으로 직접 받는다**

Controller에서 `provider.userId()`를 호출하여 추출한 후 Service에 전달한다.

```kotlin
// Controller
@PostMapping
fun createPost(
    provider: PrincipalProvider,
    @Valid @RequestBody request: CreatePostRequest,
): CommonResponse<Void> {
    postService.createPost(provider.userId(), request.toCommand())
    return CommonResponse.success(PostSuccessCode.POST_CREATED)
}

// Service — PrincipalProvider 직접 의존 없이 Long userId를 받는다
fun createPost(userId: Long, command: CreatePostCommand) { ... }
```

### 2-4. 규칙 요약

| 위치 | 규칙 |
| --- | --- |
| Controller | `provider: PrincipalProvider` 파라미터 선언, 어노테이션 없음 |
| `*Api` 인터페이스 | `@Parameter(hidden = true)` 필수 |
| Service | `Long userId`를 받음 (provider 객체 직접 의존 X) |

### 2-5. 인증이 필요 없는 API

`SecurityConfig`의 `authorizeHttpRequests`에서 permit된 경로는 토큰 없이 호출된다.
이 경우 `SecurityContextHolder`에 인증 정보가 없으므로 **`PrincipalProvider` 파라미터를 선언하지 않는다.**

```kotlin
// 인증 불필요 — PrincipalProvider 파라미터 없음
@PostMapping("/login")
fun login(@RequestBody request: LoginRequest, response: HttpServletResponse): CommonResponse<LoginResponse> { ... }

// 인증 필요 — PrincipalProvider 파라미터 있음
@PostMapping("/logout")
fun logout(provider: PrincipalProvider, httpRequest: HttpServletRequest): CommonResponse<Void> { ... }
```

### 2-6. 관련 파일

| 파일 | 역할 |
| --- | --- |
| `security/provider/PrincipalProvider.kt` | userId를 담는 data class |
| `security/provider/PrincipalProviderArgumentResolver.kt` | SecurityContext에서 꺼내 파라미터에 주입 |
| `security/UserAuthentication.kt` | `UsernamePasswordAuthenticationToken` 래퍼 |
| `security/jwt/JwtAuthFilter.kt` | 토큰 검증 후 SecurityContext에 저장 |
| `common/config/WebMvcConfig.kt` | ArgumentResolver 등록 |
