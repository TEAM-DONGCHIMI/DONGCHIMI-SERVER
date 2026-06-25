# Coding Style — 네이밍 & 클래스 작성 규칙

> 이 문서는 클래스 네이밍 컨벤션, 도메인/DTO/Entity/Repository/Service 작성 패턴, Validation 규칙을 다룬다.
> 실제 코드(Controller, Service, Repository, DTO 등) 생성/리뷰 시 참조한다.

---

## 1. 네이밍 컨벤션

### 1-1. 레이어별 클래스 네이밍

| 레이어 | 역할 | 네이밍 |
| --- | --- | --- |
| Presentation | HTTP 요청 처리 | `{Domain}Controller` |
| Presentation | 요청 객체 | `{Domain}{Action}Request` |
| Presentation | 응답 객체 | `{Domain}{Action}Response` |
| Business | 비즈니스 흐름 조율 | `{Domain}Service` |
| Implement | 데이터 조회 | `{Domain}Reader` |
| Implement | 데이터 저장 | `{Domain}Appender` |
| Implement | 데이터 삭제 | `{Domain}Remover` |
| Implement | 가공 처리 | `{Domain}Processor` |
| Implement | 검증 | `{Domain}Validator` |
| Implement | CRUD 통합 (비핵심 도메인) | `{Domain}Manager` |
| Data Access | 도메인 Repository 인터페이스 (domain 모듈) | `{Domain}Repository` |
| Data Access | Repository 구현체 (infrastructure:db 모듈) | `{Domain}RepositoryImpl` |
| Data Access | JPA Repository (infrastructure:db 모듈) | `{Domain}JpaRepository` |
| Data Access | 외부 API 클라이언트 인터페이스 | `{Domain}Client` |

### 1-2. 클래스 네이밍 예시 (User 도메인)

```
UserController
UserCreateRequest / UserCreateResponse
UserService
UserReader / UserAppender / UserRemover / UserValidator
UserRepository             ← domain 모듈, interface
UserRepositoryImpl         ← infrastructure:db 모듈, 구현체
UserJpaRepository          ← infrastructure:db 모듈, JpaRepository
```

---

## 2. 클래스 작성 규칙

### 2-1. 도메인 객체

- `data class`로 선언한다
- JPA 어노테이션을 포함하지 않는다

```kotlin
// domain 모듈
data class User(
    val id: Long,
    val name: String,
    val email: String,
)
```

### 2-2. DTO / VO

- Request/Response DTO는 `data class`로 선언한다
- 정적 팩토리 메서드 패턴을 사용하지 않고 생성자로 직접 생성한다
  - **예외**: `ApiResponse`와 같은 공통 응답 래퍼는 `private constructor` + 팩토리 메서드 패턴을 허용한다. 생성 시 `success` 여부를 강제하거나 의도를 명확히 드러내야 하는 경우에 한한다.
- Controller에서 Service로 비즈니스 로직을 위임할 때 Request DTO를 그대로 넘기지 않는다
- Request DTO를 의미 단위의 VO(Value Object)로 변환하여 Service에 전달한다
- VO는 `data class`로 선언하며 `{Domain}{Action}` 형식으로 네이밍한다

```kotlin
// Request DTO (api 모듈)
data class UserCreateRequest(
    val name: String,
    val email: String,
) {

	fun toCommand() = UserCreateCommand(name, email)
}

data class UserCreateResponse(
    val id: Long,
    val name: String,
)

// VO (core 모듈) — Controller → Service 전달 단위
data class UserCreateCommand(
    val name: String,
    val email: String,
)

// Controller
@RestController
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/users")
    fun createUser(@Valid @RequestBody request: UserCreateRequest): UserCreateResponse {
        val user = userService.createUser(request.toCommand())
        return UserCreateResponse(id = user.id, name = user.name)
    }
}
```

**공통 응답 래퍼 예시 (`ApiResponse`)**

```kotlin
// api 모듈 — 공통 응답 래퍼
data class ApiResponse<T> private constructor(
    val success: Boolean,
    val code: String = "SUCCESS",
    val message: String,
    val data: T? = null,
) {
    companion object {
        fun <T> success(data: T? = null): ApiResponse<T> =
            ApiResponse(true, message = "요청에 성공했습니다.", data = data)

        fun <T> error(code: String, message: String): ApiResponse<T> =
            ApiResponse(false, code, message)
    }
}
```

### 2-3. 레이어 간 변환 (매퍼)

- `core → api` 방향의 변환(도메인/코어 객체 → Response DTO)은 별도 매퍼 `object`에 확장 함수로 정의한다
- `core` 모듈이 `api` 타입에 의존하지 않도록 매퍼는 `api` 모듈에 위치한다
- `api → core` 방향의 변환(Request DTO → VO/Command)은 Request DTO 클래스 내부에 `toCommand()`를 정의한다

```kotlin
// api 모듈 — core → api 변환 매퍼
object PageResponseMapper {
    fun <T> PageResult<T>.toPageResponse(): PageResponse<T> =
        PageResponse(content, hasNext)
}
```

### 2-4. JPA Entity

- `domain` 모듈이 아닌 `infrastructure:db` 모듈에 선언한다
- 도메인 클래스를 인자로 받는 생성자를 제공한다
- `toDomain()` 메서드를 제공한다
- 용도에 맞는 Base Entity를 상속한다

**Base Entity 선택 기준**

| 클래스 | 제공 필드 | 사용 시점 |
| --- | --- | --- |
| `BaseCreatedTimeEntity` | `createdAt` | 생성 시각만 필요한 경우 |
| `BaseTimeEntity` | `createdAt`, `updatedAt` | 일반 엔티티 (기본값) |
| `BaseSoftDeleteEntity` | `createdAt`, `updatedAt`, `deletedAt` | 소프트 삭제가 필요한 경우 |

- 세 클래스 모두 `kr.dongchimi.infrastructure.db.common` 패키지에 위치한다
- `BaseSoftDeleteEntity`는 `BaseTimeEntity`를 상속한다
- 소프트 삭제 시 `@Transactional` 범위 안에서 `entity.delete()`를 호출하면 dirty checking으로 자동 반영된다
- 삭제된 데이터 제외 조회(`where deletedAt is null`)는 각 JpaRepository에서 직접 처리한다

```kotlin
// infrastructure:db 모듈
@Entity
@Table(name = "users")
class UserJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val name: String,

    val email: String,
) : BaseTimeEntity() {
    constructor(user: User) : this(
        id = user.id,
        name = user.name,
        email = user.email,
    )

    fun toDomain(): User = User(
        id = id,
        name = name,
        email = email,
    )
}
```

### 2-5. Repository

**domain 모듈 — 인터페이스만 선언**

```kotlin
// domain 모듈
interface UserRepository {
    fun findById(id: Long): User?
    fun save(user: User): User
}
```

**infrastructure:db 모듈 — JpaRepository**

```kotlin
// infrastructure:db 모듈
interface UserJpaRepository : JpaRepository<UserJpaEntity, Long>
```

**infrastructure:db 모듈 — 구현체**

```kotlin
// infrastructure:db 모듈
@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {

    override fun findById(id: Long): User? =
        userJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(user: User): User =
        userJpaRepository.save(UserJpaEntity(user)).toDomain()
}
```

### 2-6. Implement Layer 클래스

- 단일 책임을 갖는다
- `UserRepository` 인터페이스(domain 모듈)에만 의존한다 — JPA 구현체에 직접 의존하지 않는다
- Implement Layer가 존재해야 한다는 원칙만 지키면 되고, 세부 분리 방식은 도메인 특성에 따라 느슨하게 적용한다

**핵심 도메인 — Reader/Appender/Remover/Validator 등으로 세분화**

CRUD 각각의 책임이 명확히 분리되어야 하는 핵심 도메인은 역할별로 클래스를 나눈다.

```kotlin
// domain 모듈
@Component
class UserReader(
    private val userRepository: UserRepository,
) {
    fun read(id: Long): User =
        userRepository.findById(id) ?: throw BusinessException(UserErrorCode.USER_NOT_FOUND)
}

@Component
class UserAppender(
    private val userRepository: UserRepository,
) {
    fun append(name: String, email: String): User =
        userRepository.save(User(name, email))
}
```

**비핵심 도메인 — `{Domain}Manager`로 통합**

CRUD 책임을 세분화할 필요가 없는 단순/비핵심 도메인은 `{Domain}Manager` 하나로 묶어서 처리할 수 있다. 이 경우에도 Service가 Repository를 직접 참조하지 않고 Implement Layer(Manager)를 통해서만 접근한다는 규칙은 유지한다.

```kotlin
// domain 모듈
@Component
class TagManager(
    private val tagRepository: TagRepository,
) {
    fun read(id: Long): Tag =
        tagRepository.findById(id) ?: throw BusinessException(TagErrorCode.TAG_NOT_FOUND)

    fun append(name: String): Tag =
        tagRepository.save(Tag(name = name))

    fun remove(id: Long) {
        tagRepository.deleteById(id)
    }
}
```

### 2-7. Business Layer (Service)

- `Repository`를 직접 참조하지 않는다
- Implement Layer의 클래스들을 조합하여 비즈니스 흐름을 표현한다

```kotlin
// core 모듈
@Service
class UserService(
    private val userReader: UserReader,
    private val userAppender: UserAppender,
    private val userValidator: UserValidator,
) {
    fun append(command: UserCreate): User {
        userValidator.validateEmail(command.email)
        return userAppender.append(command.name, command.email)
    }

    fun read(id: Long): User {
        return userReader.read(id)
    }
}
```

---

## 3. Validation (유효성 검증)

- Request DTO 필드에 Jakarta Validation 어노테이션을 사용한다 (`@NotBlank`, `@NotNull`, `@Size` 등)
- 검증 메시지는 한글로 작성한다

```kotlin
data class UserCreateRequest(
    @field:NotBlank(message = "이름은 필수로 입력해 주세요.")
    val name: String,

    @field:NotBlank(message = "이메일은 필수로 입력해 주세요.")
    val email: String,
)
```

- Controller 메서드 파라미터에 `@Valid`를 선언하여 검증을 활성화한다

```kotlin
@PostMapping("/users")
fun createUser(@Valid @RequestBody request: UserCreateRequest): UserCreateResponse { ... }
```

- 검증 실패 시 `MethodArgumentNotValidException` → `GlobalExceptionHandler`에서 400 응답 처리 (자세한 내용은 `error-handling.md` 참고)
- 에러 메시지는 첫 번째 `FieldError`의 message를 반환한다
