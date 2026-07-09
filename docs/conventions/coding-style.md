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
| Presentation | 여러 도메인 Service 조회 조합 | `{Feature}QueryFacade` |
| Business | 비즈니스 흐름 조율 | `{Domain}Service` |
| Implement | 데이터 조회 | `{Domain}Reader` |
| Implement | 데이터 저장 | `{Domain}Appender` |
| Implement | 데이터 삭제 | `{Domain}Remover` |
| Implement | 가공 처리 | `{Domain}Processor` |
| Implement | 검증 | `{Domain}Validator` |
| Implement | CRUD 통합 (비핵심 도메인) | `{Domain}Manager` |
| Data Access | 도메인 Repository 인터페이스 (core 모듈) | `{Domain}Repository` |
| Data Access | Repository 구현체 (infrastructure:db 모듈) | `{Domain}RepositoryImpl` |
| Data Access | JPA Repository (infrastructure:db 모듈) | `{Domain}JpaRepository` |
| Data Access | 외부 API 클라이언트 인터페이스 | `{Domain}Client` |

### 1-2. 클래스 네이밍 예시 (User 도메인)

```
UserController
UserCreateRequest / UserCreateResponse
UserService
UserReader / UserAppender / UserRemover / UserValidator
UserRepository             ← core 모듈, interface
UserRepositoryImpl         ← infrastructure:db 모듈, 구현체
UserJpaRepository          ← infrastructure:db 모듈, JpaRepository
```

---

## 2. 클래스 작성 규칙

### 2-1. 도메인 객체

- `data class`로 선언한다
- JPA 어노테이션을 포함하지 않는다
- 의미적으로 묶이는 필드가 여러 개면 평평하게 나열하지 않고 VO로 그룹화한다(예: 가격/할인율은 `Price`, 시작일/종료일은 `DiscountPeriod`). VO도 `data class`로 선언하며 같은 도메인 패키지에 둔다

```kotlin
// core 모듈
data class User(
    val id: Long,
    val name: String,
    val email: String,
)

// 연관 필드를 VO로 묶은 예시
data class Price(
    val originalPrice: BigDecimal,
    val discountedPrice: BigDecimal,
) {
    fun discountRate(): Int =
        originalPrice.subtract(discountedPrice)
            .divide(originalPrice, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal(100))
            .toInt()
}

data class Product(
    val id: Long,
    val price: Price,
    // ...
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

### 2-3. 레이어 간 변환

- `core → api` 방향의 변환(도메인/코어 객체 → Response DTO)은 별도 매퍼 `object`를 만들지 않고, Response DTO에 도메인/코어 객체를 인자로 받는 보조 생성자(secondary constructor)를 정의한다
- Response DTO가 `api` 모듈에 있으므로 `core` 모듈은 `api` 타입에 의존하지 않는다
- `api → core` 방향의 변환(Request DTO → VO/Command)은 Request DTO 클래스 내부에 `toCommand()`를 정의한다

```kotlin
// api 모듈 — core → api 변환은 Response DTO 보조 생성자로
data class CursorSliceResponse<T>(
    val content: List<T>,
    val hasNext: Boolean,
    val nextCursor: Long? = null,
) {
    constructor(cursorSliceResult: CursorSliceResult<T>) : this(
        content = cursorSliceResult.content,
        hasNext = cursorSliceResult.hasNext,
        nextCursor = cursorSliceResult.nextCursor,
    )
}
```

**목록 조회 응답 — 커서 기반 vs 오프셋 기반**

목록을 페이지네이션할 때는 조회 방식에 맞는 타입을 선택한다. 둘 다 `core.common` 패키지의 `{X}Result`(core 모듈)와 `api.core.common.dto` 패키지의 `{X}Response`(api 모듈, 2-3절 변환 패턴을 따르는 보조 생성자 포함) 쌍으로 구성된다.

| 방식 | 사용 시점 | 타입 |
| --- | --- | --- |
| 커서/슬라이스 기반 | 무한 스크롤 등 다음 페이지 존재 여부와 다음 커서만 필요한 경우 | `CursorSliceResult<T>` / `CursorSliceResponse<T>` |
| 오프셋 기반 | 페이지 번호·전체 개수·전체 페이지 수가 필요한 경우 | `PageResult<T>` / `PageResponse<T>` |

커서 기반 목록은 응답 필드명이 항상 `content`/`hasNext`/`nextCursor`로 통일된다. 도메인별로 `{Domain}ListResponse`를 새로 만들지 않는다.
`nextCursor`는 다음 페이지가 없으면 `null`이며, 값을 채우는 책임은 슬라이싱을 수행하는 Implement Layer(`{Domain}Finder` 등)에 있다.

```kotlin
// core 모듈 — 커서/슬라이스 기반 (다음 페이지 존재 여부 + 다음 커서)
data class CursorSliceResult<T>(
    val content: List<T>,
    val hasNext: Boolean,
    val nextCursor: Long? = null,
)

// core 모듈 — 오프셋 기반 (전체 개수/페이지 수 필요)
data class PageResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalCount: Long,
    val totalPage: Int,
)
```

### 2-4. JPA Entity

- `core` 모듈이 아닌 `infrastructure:db` 모듈에 선언한다
- 도메인 클래스를 인자로 받는 생성자를 제공한다
- `toDomain()` 메서드를 제공한다
- 용도에 맞는 Base Entity를 상속한다

**Base Entity 선택 기준**

| 클래스 | 제공 필드 | 사용 시점 |
| --- | --- | --- |
| `BaseCreatedTimeEntity` | `createdAt` | 생성 시각만 필요한 경우 |
| `BaseTimeEntity` | `createdAt`, `updatedAt` | 일반 엔티티 (기본값) |
| `BaseSoftDeleteEntity` | `createdAt`, `updatedAt`, `deletedAt` | 소프트 삭제가 필요한 경우 |

- 세 클래스 모두 `kr.dongchimi.db.common` 패키지에 위치한다
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

**다른 aggregate를 참조하는 컬럼**

- 다른 도메인(aggregate)을 참조하는 컬럼은 `@ManyToOne`/`@OneToOne`/`@JoinColumn` 같은 JPA 연관관계 어노테이션을 쓰지 않고 단순 `Long` 필드(예: `ownerId`, `marketId`)로 선언한다
- DB에도 FK 제약조건을 걸지 않는다(Flyway 마이그레이션에도 `REFERENCES`를 추가하지 않음) — aggregate 간 결합도를 낮추고 불필요한 지연 로딩/N+1을 방지하기 위함

**JSONB 컬럼**

- Hibernate 7 내장 `@JdbcTypeCode(SqlTypes.JSON)` + `@Column(columnDefinition = "jsonb")`를 필드 타입에 직접 붙이면, 클래스패스의 Jackson을 통해 `String`이 아닌 `data class`로도 자동 직렬화/역직렬화된다

```kotlin
@JdbcTypeCode(SqlTypes.JSON)
@Column(columnDefinition = "jsonb")
val businessHours: BusinessHours? = null,
```

**부모와 PK를 공유하는 1:1 보조 테이블 (메타데이터 등)**

- `market_metadata`처럼 부모 테이블과 PK를 공유하는 보조 테이블은 `@GeneratedValue` 없이 부모와 동일한 값을 그대로 `@Id`에 사용한다

```kotlin
@Entity
@Table(name = "product_metadata")
class ProductMetadataJpaEntity(
    @Id
    @Column(name = "product_id")
    val id: Long,

    @Column(nullable = false)
    val viewCount: Int,
)
```

### 2-5. Repository

**core 모듈 — 인터페이스만 선언**

```kotlin
// core 모듈
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
- `UserRepository` 인터페이스(core 모듈)에만 의존한다 — JPA 구현체에 직접 의존하지 않는다
- Implement Layer가 존재해야 한다는 원칙만 지키면 되고, 세부 분리 방식은 도메인 특성에 따라 느슨하게 적용한다

**핵심 도메인 — Reader/Appender/Remover/Validator 등으로 세분화**

CRUD 각각의 책임이 명확히 분리되어야 하는 핵심 도메인은 역할별로 클래스를 나눈다.

```kotlin
// core 모듈
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
// core 모듈
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

### 2-8. QueryFacade (여러 도메인 조회 조합)

- 하나의 API 응답이 **단일 Service로 표현되지 않는 교차-도메인 조회**를 필요로 할 때 사용한다 (예: 홈 화면처럼 마트 + 상품처럼 서로 다른 도메인의 조회 결과를 한 응답에 합쳐야 하는 경우).
- **Presentation Layer**(`api:*` 모듈)에 `{Feature}QueryFacade`로 둔다. `core` 모듈에는 두지 않는다 — core는 도메인별 경계를 유지하고, 교차-도메인 조합은 응답을 만드는 presentation의 책임으로 본다.
- 여러 도메인의 `{Domain}Service` **조회 메서드만** 조합한다. Repository/Reader를 직접 참조하지 않는다 — 반드시 Service를 통해서만 접근한다.
- 응답 DTO(`{Domain}{Action}Response`)를 **직접 조립**한다. 별도의 core 집계 VO를 만들지 않는다.
- Controller는 개별 Service가 아닌 Facade 하나만 참조한다: `Controller → QueryFacade → 각 도메인 Service → Implement → Repository`.
- 조회 전용이므로 상태 변경이 없다. `@Transactional(readOnly = true)`를 사용한다.
- **단일 도메인 조회에는 QueryFacade를 만들지 않는다.** 한 도메인의 `{Domain}Service` 조회 메서드 여러 개(예: 목록 조회 + 카운트 조회)를 조합해 응답을 만드는 경우도 교차-도메인이 아니므로 QueryFacade 대상이 아니다 — Controller가 해당 Service를 직접 참조해 조합하고 응답 DTO를 조립한다. QueryFacade는 **서로 다른 도메인의 Service 두 개 이상**을 조합해야 할 때만 만든다.

```kotlin
// 단일 도메인 조회 조합 — QueryFacade 없이 Controller에서 직접 처리
@RestController
@RequestMapping("/v1/owners/markets/{marketId}/products")
class OwnerProductController(
    private val preparedProductService: PreparedProductService,
) : OwnerProductApi {
    @GetMapping("/draft")
    override fun getDrafts(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        request: PreparedProductDraftSearchRequest,
        pageOffsetRequest: PageOffsetRequest,
    ): ApiResponse<OwnerPreparedProductDraftListResponse> {
        val counts = preparedProductService.getDraftCounts(apiUser.userId, marketId)
        val preparedProducts =
            preparedProductService.getDrafts(apiUser.userId, marketId, request.toSearchCondition(), pageOffsetRequest.toPageOffset())

        return ApiResponse.success(OwnerPreparedProductDraftListResponse(counts, preparedProducts))
    }
}
```

```kotlin
// api:owner-api / kr.dongchimi.api.owner.home
@Component
class OwnerHomeQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
) {
    @Transactional(readOnly = true)
    fun getHome(ownerId: Long): OwnerHomeResponse {
        val market = marketService.findByOwnerId(ownerId)
            ?: return OwnerHomeResponse.empty()

        val dailyProducts = productService.getActiveProducts(market.id, DealType.DAILY)
        val periodicProducts = productService.getActiveProducts(market.id, DealType.PERIODIC)

        return OwnerHomeResponse(
            dailyProducts = dailyProducts.map { HomeProductResponse(it) },
            periodicProducts = periodicProducts.map { HomeProductResponse(it) },
        )
    }
}

// Controller
@RestController
@RequestMapping("/v1/owners/home")
class OwnerHomeController(
    private val ownerHomeQueryFacade: OwnerHomeQueryFacade,
) : OwnerHomeApi {
    @GetMapping
    override fun getHome(owner: OwnerApiUser): ApiResponse<OwnerHomeResponse> =
        ApiResponse.success(ownerHomeQueryFacade.getHome(owner.userId))
}
```

---

## 3. Validation (유효성 검증)

- Jakarta Validation 어노테이션(`@NotBlank`, `@Valid` 등)과 `spring-boot-starter-validation`을 사용하지 않는다. `api:*` 모듈의 `spring-boot-starter-webmvc`에서 `spring-boot-validation`(hibernate-validator)을 명시적으로 제외한다(`dongchimi.api-module-conventions.gradle.kts`, `api/core-api/build.gradle.kts`).
- 대신 `api:core-api`의 `validate(condition, errorMessage)` 헬퍼(`error-handling.md` 3절)를 Request DTO의 `toCommand()` 안에서 호출해 검증한다
- 검증 메시지는 한글로 작성한다

```kotlin
data class UserCreateRequest(
    val name: String,
    val email: String,
) {
    fun toCommand(): UserCreateCommand {
        validate(name.isNotBlank()) { "이름은 필수로 입력해 주세요." }
        validate(email.isNotBlank()) { "이메일은 필수로 입력해 주세요." }

        return UserCreateCommand(name, email)
    }
}
```

- Controller는 `@Valid`를 선언하지 않는다 — `toCommand()` 호출 시점에 검증이 함께 이뤄진다

```kotlin
@PostMapping("/users")
fun createUser(@RequestBody request: UserCreateRequest): UserCreateResponse {
    val user = userService.createUser(request.toCommand())
    return UserCreateResponse(id = user.id, name = user.name)
}
```

- 검증 실패 시 `validate()`가 `InvalidInputException`(`CoreException` 상속)을 던지고, 기존 `GlobalExceptionHandler`의 `CoreException` 핸들러가 그대로 400 응답을 처리한다(자세한 내용은 `error-handling.md` 참고)
- `toCommand()`가 아닌 곳(예: `init` 블록)에서 검증하지 않는다 — Jackson 역직렬화 도중 던진 예외는 `HttpMessageNotReadableException` 등으로 감싸져 `GlobalExceptionHandler`의 `CoreException` 핸들러가 잡지 못하고 500으로 응답한다
