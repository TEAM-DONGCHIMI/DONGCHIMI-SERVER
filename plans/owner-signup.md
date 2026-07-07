# 사장님(OWNER) 회원가입 API 구현 계획

## 목표

이메일/비밀번호 기반 사장님 회원가입 API를 구현한다. (로그인은 후속 작업 — 문서 하단 참고)

- 엔드포인트: `POST /v1/owners/auth/signup` (인증 불필요, `permitAll`)
- 요청: `email`, `password`
- 동작: 이메일 중복 검사 → 비밀번호 해싱(BCrypt) → `owners` 저장 → 생성된 `ownerId`, `email` 반환
- **회원가입 시 토큰은 발급하지 않는다.** (자동 로그인 X — 로그인은 별도 API로 분리)

## 확정된 결정

| 항목 | 결정 |
| --- | --- |
| 가입 필드 | `email` + `password`만. 기존 `owners` 스키마 그대로 사용 → **Flyway 마이그레이션 불필요** |
| 비밀번호 해싱 | `core`에 `PasswordEncoder` 인터페이스(포트), `gateway:auth`에 BCrypt 구현체 (기존 `TokenProvider` → `JwtTokenProvider` 패턴과 동일) |
| 회원가입 응답 | 토큰 미발급. 생성된 `ownerId` + `email` 반환 (API 문서 기준) |

## 이미 갖춰진 것 (재사용)

- `owners` 테이블: `owner_id`, `email`, `password`, soft-delete 컬럼 (V1) + active email unique index (V4)
- `core/owner/Owner.kt` (`id`, `email`, `password`), `OwnerJpaEntity`, `OwnerRepositoryImpl`(`findById`, `save`)
- `AuthTokenIssuer.issue(userId, roles)` — 로그인 단계에서 `setOf(Role.OWNER.name)`으로 재사용 예정
- `RefreshTokenCookieFactory` (`api:core-api`) — 로그인 단계에서 재사용 예정
- 에러 응답/검증 인프라: `ApiResponse`, `GlobalExceptionHandler`, `validate(...)`, `@ApiErrorCodes`

---

## 구현 항목 (모듈 → 레이어 순)

### 1. `core` — 도메인 & 비즈니스

레이어 규칙: **Controller → Service → Implement(Reader/Appender) → Repository 인터페이스**. Service는 Repository를 직접 참조하지 않는다.

- **`core/auth/PasswordEncoder.kt`** *(신규 · 공통 포트)*
  ```kotlin
  interface PasswordEncoder {
      fun encode(rawPassword: String): String
      fun matches(rawPassword: String, encodedPassword: String): Boolean
  }
  ```
  - `auth` 패키지에 두어 USER/OWNER/ADMIN 모두 재사용. Spring Security 의존 없음.

- **`core/owner/OwnerErrorCode.kt`** *(신규)*
  ```kotlin
  enum class OwnerErrorCode(override val status: Int, override val message: String) : ErrorCode {
      DUPLICATE_EMAIL(ErrorStatus.CONFLICT, "이미 가입된 이메일입니다."),
      // 로그인 단계에서 INVALID_CREDENTIALS(UNAUTHORIZED) 추가 예정
  }
  ```

- **`core/owner/exception/DuplicateEmailException.kt`** *(신규)*
  - `UserErrorCode.DUPLICATE_SOCIAL_ACCOUNT`용 `DuplicateSocialAccountException` 패턴을 그대로 미러링.
  ```kotlin
  class DuplicateEmailException(
      cause: Throwable? = null,
  ) : CoreException(OwnerErrorCode.DUPLICATE_EMAIL) {
      init { cause?.let(::initCause) }
  }
  ```
  - unique index 위반 경합(버튼 연속 클릭 등)을 `RepositoryImpl`에서 이 예외로 변환한다(아래 3번). `GlobalExceptionHandler`의 `handleCoreException`이 그대로 409로 매핑하므로 핸들러 추가 수정 불필요.

- **`core/owner/OwnerSignupCommand.kt`** *(신규 · VO)*
  - `email: String`, `password: String`. Controller의 Request DTO를 `toCommand()`로 변환해 전달.

- **`core/owner/OwnerReader.kt`** *(신규 · Implement Layer)*
  - `fun existsByEmail(email: String): Boolean` — 중복 검사용. (`UserReader` 패턴 참고)

- **`core/owner/OwnerAppender.kt`** *(신규 · Implement Layer)*
  - `@Transactional fun append(command: OwnerSignupCommand): Owner`
  - 내부에서 `passwordEncoder.encode(command.password)`로 해싱 후 `Owner` 저장. (`UserAppender` 패턴 참고)

- **`core/owner/OwnerSignupService.kt`** *(신규 · Service)*
  ```kotlin
  @Service
  class OwnerSignupService(
      private val ownerReader: OwnerReader,
      private val ownerAppender: OwnerAppender,
  ) {
      fun signup(command: OwnerSignupCommand): Owner {
          if (ownerReader.existsByEmail(command.email)) {
              throw CoreException(OwnerErrorCode.DUPLICATE_EMAIL)
          }
          return ownerAppender.append(command)
      }
  }
  ```
  - **2단 방어**: (1차) `ownerReader.existsByEmail`로 일반 케이스 차단 → 대부분 여기서 걸러짐. (2차) 짧은 경합 구간(버튼 연속 클릭 등 동시 요청)은 DB의 active email unique index(V4, `uq_owners_email_active`)가 최종 방어선. 이때 발생하는 `DataIntegrityViolationException`은 `OwnerRepositoryImpl`에서 `DuplicateEmailException`으로 변환한다(아래 3번). → 두 경로 모두 동일하게 `409 DUPLICATE_EMAIL`로 응답.

- **`core/owner/OwnerRepository.kt`** *(수정)*
  - `fun findByEmail(email: String): Owner?` 추가. (`existsByEmail`은 Reader에서 `findByEmail(...) != null`로 처리하거나 repository에 `existsByEmail` 추가 — 구현 시 택1, 기존 `findBy*` 스타일에 맞춰 `findByEmail` 권장)

### 2. `gateway:auth` — 비밀번호 해싱 구현체

- **`gateway/auth/security/BCryptPasswordEncoderAdapter.kt`** *(신규)*
  ```kotlin
  @Component
  class BCryptPasswordEncoderAdapter : PasswordEncoder {
      private val delegate = BCryptPasswordEncoder()
      override fun encode(rawPassword: String) = delegate.encode(rawPassword)
      override fun matches(rawPassword: String, encodedPassword: String) =
          delegate.matches(rawPassword, encodedPassword)
  }
  ```
  - `spring-security-crypto`의 `BCryptPasswordEncoder`를 감싼다. 별도 `@Bean` 등록 없이 어댑터 내부에서 인스턴스화.
  - `bootstrap`이 `kr.dongchimi` 전체를 스캔하므로 `core`의 서비스에 자동 주입됨. (`JwtTokenProvider`와 동일 방식)
  - 클래스명/패키지는 팀 취향에 맞게 조정 가능 (`security` 패키지 배치 권장).

### 3. `infrastructure:db` — Repository 구현

- **`db/owner/OwnerJpaRepository.kt`** *(수정)*
  - `fun findByEmailAndDeletedAtIsNull(email: String): OwnerJpaEntity?` 추가.

- **`db/owner/OwnerRepositoryImpl.kt`** *(수정)*
  - `findByEmail` 구현: `ownerJpaRepository.findByEmailAndDeletedAtIsNull(email)?.toDomain()`
  - `save`에 동시 가입 경합 방어 추가 — `UserRepositoryImpl.save`와 동일 패턴:
    ```kotlin
    override fun save(owner: Owner): Owner =
        try {
            ownerJpaRepository.save(OwnerJpaEntity(owner)).toDomain()
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isOwnerEmailUniqueViolation()) {
                throw DuplicateEmailException(cause = exception)
            }
            throw exception
        }

    private fun DataIntegrityViolationException.isOwnerEmailUniqueViolation(): Boolean =
        mostSpecificCause.message?.contains(OWNER_EMAIL_UNIQUE_INDEX) == true

    companion object {
        private const val OWNER_EMAIL_UNIQUE_INDEX = "uq_owners_email_active"
    }
    ```
  - **동작 근거**: `owner_id`가 IDENTITY(auto-increment)라 Hibernate가 `save` 시점에 INSERT를 즉시 실행 → 제약 위반이 `save()` 호출 안에서 바로 터져 catch 가능. (user 소셜 계정 처리와 동일한 이유로 flush 강제 없이 동작 — `UserSocialAccountUniqueConstraintTest`가 이 동작을 이미 검증)

- **Flyway: 불필요** (스키마 이미 존재)

### 4. `api:owner-api` — Controller & DTO

- **`api/owner/auth/request/OwnerSignupRequest.kt`** *(신규)*
  - `email`, `password` + `@Schema`. `fun toCommand(): OwnerSignupCommand`에서 `validate(...)`로 검증 (한글 메시지, 코드는 모두 공통 `INVALID_INPUT`). API 문서(`docs/api/사장님_회원가입.md`)의 검증 규칙/메시지와 1:1로 맞춘다:
    - 이메일 형식 → `"올바르지 않은 이메일 형식입니다."`
    - 비밀번호 6~20자 → `"비밀번호는 6~20자로 입력해주세요."` (길이 임계값 6/20은 **DTO에 리터럴로 둔다** — 팀 결정)
    - 비밀번호 공백 불가 → `"비밀번호에 공백을 포함할 수 없습니다."`
    - 비밀번호 한글 불가 → `"비밀번호에 한글을 포함할 수 없습니다."`

- **`api/owner/auth/response/OwnerSignupResponse.kt`** *(신규)*
  - `ownerId: Long`, `email: String` + `@Schema`. (API 문서 응답 `data`와 일치)

- **`api/owner/auth/OwnerSignupApi.kt`** *(신규 · Swagger 인터페이스)*
  - `@Tag`, `@Operation`, `@ApiErrorCodes(CommonErrorCode::class, OwnerErrorCode::class)`.

- **`api/owner/auth/OwnerSignupController.kt`** *(신규)*
  ```kotlin
  @RestController
  @RequestMapping("/v1/owners/auth/signup")
  class OwnerSignupController(
      private val ownerSignupService: OwnerSignupService,
  ) : OwnerSignupApi {
      @PostMapping
      override fun signup(@RequestBody request: OwnerSignupRequest): ApiResponse<OwnerSignupResponse> {
          val owner = ownerSignupService.signup(request.toCommand())
          return ApiResponse.success(OwnerSignupResponse(owner.id, owner.email))
      }
  }
  ```
  - `permitAll` 경로이므로 `OwnerApiUser` 파라미터 없음.
  - `api:owner-api`는 이미 `core-api`/`core`/`gateway:auth(runtimeOnly)` 의존 → 추가 gradle 변경 불필요.

### 5. `gateway:auth` — 공개 엔드포인트 등록

- **`gateway/auth/PublicEndpoints.kt`** *(수정)*
  - `AUTH` 배열에 `"/v1/owners/auth/signup"` 추가. (로그인 단계에서 `"/v1/owners/auth/login"`도 추가)
  - `SecurityConfig`에서 `PublicEndpoints.AUTH`의 `permitAll`이 `OWNER_API_PATTERN(/v1/owners/**)`보다 **먼저** 선언되므로, 해당 경로만 공개로 열림. (순서 규칙 준수 — 별도 `SecurityConfig` 수정 불필요)

---

## 테스트 계획

| 모듈 | 테스트 | 검증 내용 |
| --- | --- | --- |
| `core` | `OwnerSignupServiceTest` | 중복 이메일 시 `CoreException(DUPLICATE_EMAIL)`, 정상 시 Appender 호출/저장 |
| `core` | `OwnerAppenderTest` | 비밀번호가 원문이 아닌 해시로 저장되는지 (`PasswordEncoder` mock/stub) |
| `gateway:auth` | `BCryptPasswordEncoderAdapterTest` | `encode` 결과가 원문과 다르고 `matches`가 true |
| `api:owner-api` | `OwnerSignupControllerTest` | MockMvc — 200 SUCCESS(`ownerId`+`email`), 중복 시 `DUPLICATE_EMAIL`·검증 실패 시 `INVALID_INPUT` 매핑 |
| `api:owner-api` | `OwnerSignupRequestTest` | `toCommand` 검증 규칙 4종 (이메일 형식 / 6~20자 / 공백 / 한글) — 각각 `INVALID_INPUT` + 정확한 메시지 |
| `infrastructure:db` | `OwnerRepositoryImplTest` (또는 기존 `OwnerEmailUniqueConstraintTest` 보강) | active email unique 위반 시 `save`가 `DuplicateEmailException`으로 변환하는지 (`UserSocialAccountUniqueConstraintTest` 패턴) |
| `api:owner-api` | `OwnerSignupApiAnnotationTest` | `@ApiErrorCodes` 등 어노테이션 (기존 `OAuthLoginApiAnnotationTest` 패턴) |
| `infrastructure:db` | (기존 `OwnerEmailUniqueConstraintTest` 활용) | active email unique 제약 — 필요 시 `findByEmail` 케이스 보강 |

---

## 작업 순서 (커밋 단위 제안)

1. `core`: `PasswordEncoder` 포트 + `OwnerErrorCode` + `DuplicateEmailException` + `OwnerSignupCommand`
2. `gateway:auth`: `BCryptPasswordEncoderAdapter`
3. `core`: `OwnerRepository.findByEmail` + `OwnerReader` + `OwnerAppender` + `OwnerSignupService` (+ 단위 테스트)
4. `infrastructure:db`: `OwnerJpaRepository`에 `findByEmailAndDeletedAtIsNull` + `OwnerRepositoryImpl`에 `findByEmail` 및 `save` 경합 방어(`DuplicateEmailException` 변환) (+ 제약 위반 테스트)
5. `api:owner-api`: Request/Response/Api/Controller + `PublicEndpoints`에 경로 추가 (+ MockMvc 테스트)
6. `./gradlew build` (ktlint 포함) 통과 확인

### Git
- 브랜치: `feat/#{이슈번호}-owner-signup`
- 커밋: `feat: 사장님 회원가입 API 구현` 등 (`type: 한글 제목`)
- PR: `[Feat/#{이슈번호}] 사장님 회원가입 API`, Squash Merge, `main` 직접 push 금지

---

## 확정된 결정 (이전 열린 항목 정리)

- **비밀번호 검증** — 규칙(6~20자, 공백/한글 불가)·메시지는 API 문서 기준. 길이 임계값 6/20은 **DTO에 리터럴**로 둔다(팀 결정, `@ConfigurationProperties`로 빼지 않음).
- **동시 가입 경합** — `existsByEmail` 1차 차단 + `uq_owners_email_active` 위반을 `OwnerRepositoryImpl`에서 `DuplicateEmailException`으로 변환(2차). 두 경로 모두 `409 DUPLICATE_EMAIL`. `UserRepositoryImpl` 선례와 동일.
- **응답 형태** — `data`에 `ownerId` + `email` 반환(API 문서).

> 남은 열린 항목 없음. 이대로 구현 진행 가능.

---

## 다음 단계: 로그인 (별도 작업)

회원가입 완료 후 진행. 재사용 요소가 많아 규모는 작다.

- `POST /v1/owners/auth/login` (`PublicEndpoints.AUTH`에 추가)
- `OwnerErrorCode.INVALID_CREDENTIALS(UNAUTHORIZED)` 추가
- `OwnerLoginService`: `OwnerReader.readByEmail` → `passwordEncoder.matches` 검증 → `authTokenIssuer.issue(owner.id, setOf(Role.OWNER.name))`
- 응답: access token(바디) + refresh token(쿠키, `RefreshTokenCookieFactory`) — 기존 `OAuthLoginController`와 동일 형태
