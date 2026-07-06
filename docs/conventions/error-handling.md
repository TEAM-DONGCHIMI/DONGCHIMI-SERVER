# Error Handling — 예외 처리 & Swagger 에러 문서화

> 이 문서는 ErrorCode/CoreException 체계, GlobalExceptionHandler, Swagger 에러 응답 문서화 방식을 다룬다.
> 새 에러 코드 추가, 예외 처리 로직 작업 시 참조한다.

---

## 1. ErrorCode 인터페이스 & ErrorStatus

`ErrorCode` 인터페이스는 `core` 모듈에, HTTP 상태 코드 상수는 `common` 모듈의 `ErrorStatus`에 둔다.
Spring의 `HttpStatus`를 직접 사용하지 않아 `core`/`common` 모듈의 프레임워크 의존성을 제거한다.

```kotlin
// core/src/main/kotlin/kr/dongchimi/core/common/exception/ErrorCode.kt
interface ErrorCode {
    val name: String
    val status: Int
    val message: String
}
```

```kotlin
// common/src/main/kotlin/kr/dongchimi/common/exception/ErrorStatus.kt
object ErrorStatus {
    const val INTERNAL_SERVER_ERROR = 500
    const val UNAUTHORIZED = 401
    const val NOT_FOUND = 404
    const val BAD_REQUEST = 400
    const val CONFLICT = 409
    const val FORBIDDEN = 403
}
```

---

## 2. CommonErrorCode & 도메인 ErrorCode

공통 에러 코드는 `core` 모듈의 `CommonErrorCode`에 정의한다.
도메인별 에러 코드는 별도 enum으로 추가한다: `UserErrorCode`, `AuthErrorCode` 등

```kotlin
// core/src/main/kotlin/kr/dongchimi/core/common/exception/CommonErrorCode.kt
enum class CommonErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    INVALID_INPUT(ErrorStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
    UNAUTHORIZED(ErrorStatus.UNAUTHORIZED, "인증이 필요합니다."),
    INTERNAL_SERVER_ERROR(ErrorStatus.INTERNAL_SERVER_ERROR, "내부 서버 오류입니다. 다시 시도해 주세요."),
}
```

```kotlin
// 도메인별 enum 예시
enum class UserErrorCode(
    override val status: Int,
    override val message: String,
) : ErrorCode {
    USER_NOT_FOUND(ErrorStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
}
```

`name`은 enum의 내장 프로퍼티로 인터페이스를 자동으로 충족하므로 별도 선언하지 않는다.

---

## 3. CoreException 계층

```kotlin
// core/src/main/kotlin/kr/dongchimi/core/common/exception/CoreException.kt
open class CoreException(
    val errorCode: ErrorCode,
    message: String = errorCode.message,
    formatArgs: List<Any> = emptyList()
) : RuntimeException(
    if (formatArgs.isEmpty()) message else message.format(*formatArgs.toTypedArray())
)
```

도메인/기능별 예외는 `CoreException`을 상속한다.

```kotlin
// api/src/main/kotlin/kr/dongchimi/api/common/exception/InvalidInputException.kt
class InvalidInputException(message: String) : CoreException(CommonErrorCode.INVALID_INPUT, message)

fun validate(condition: Boolean, errorMessage: () -> String) {
    if (!condition) throw InvalidInputException(errorMessage())
}
```

---

## 4. GlobalExceptionHandler

MDC에서 `requestId`, `userId`를 읽어 로그에 포함한다.
`CoreException`은 `warn`, 그 외 예외는 `error`로 로깅한다.

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CoreException::class)
    fun handleCoreException(exception: CoreException): ResponseEntity<ApiResponse<Any>> {
        logger.warn {
            "[requestId=${MDC.get(REQUEST_ID)}, userId=${MDC.get(USER_ID)}] (${exception.errorCode.name}) ${exception.message}"
        }
        return ResponseEntity
            .status(exception.errorCode.status)
            .body(ApiResponse.error(exception))
    }

    @ExceptionHandler(Exception::class)
    fun handleInternalException(exception: Exception): ResponseEntity<ApiResponse<Any>> {
        logger.error(exception) {
            "[requestId=${MDC.get(REQUEST_ID)}, userId=${MDC.get(USER_ID)}] ${exception.message}"
        }
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR))
    }
}
```

---

## 5. ApiResponse 에러 팩토리

`ApiResponse.error()`는 `CoreException` 또는 `ErrorCode`를 받는다. 원시 문자열을 직접 전달하지 않는다.

```kotlin
ApiResponse.error(exception)          // CoreException — errorCode.name + exception.message 사용
ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR)  // ErrorCode 직접 전달
```

---

## 6. Swagger 에러 문서화

커스텀 어노테이션 `@ApiErrorCodes`(`api:core-api`)로 에러 응답을 문서화한다.
개별 코드가 아닌 **`ErrorCode` enum 클래스를 통째로** 참조한다 — enum 상수를 섞어 배열로 담는 문법은 애초에 컴파일이 불가능하고, 개별 코드만 타입 안전하게 선택하려면 `ErrorCode`를 `enum class` 대신 `object` 계층으로 바꾸거나 별도 컴파일 검증기를 붙여야 해서(컨벤션 전면 변경) 배보다 배꼽이다. enum 하나에 코드가 너무 많아지면, 그때 enum을 목적에 맞게 쪼갠다.

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorCodes(
    vararg val value: KClass<out ErrorCode>,
)
```

```kotlin
@ApiErrorCodes(UserErrorCode::class, AuthErrorCode::class)
@PostMapping("/login")
fun login(...): ApiResponse<LoginResponse> { ... }
```

`ApiErrorCodesCustomizer`(`OperationCustomizer` 구현체, `@Component`)가 넘어온 enum들의 모든 상수를 펼쳐 OpenAPI 응답에 추가한다. springdoc이 `OperationCustomizer` 타입 빈을 자동 감지하므로 별도 등록은 필요 없다.

```kotlin
@Component
class ApiErrorCodesCustomizer : OperationCustomizer {
    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        val annotation = handlerMethod.getMethodAnnotation(ApiErrorCodes::class.java) ?: return operation

        val errorCodes: List<ErrorCode> =
            annotation.value.flatMap { it.java.enumConstants?.toList() ?: emptyList() }

        // 같은 HTTP status는 하나의 응답으로 묶고, 코드별 example을 여러 개 단다
        errorCodes.groupBy { it.status }.forEach { (status, codes) ->
            // status를 key로 하는 OpenAPI 응답에 코드별 example 추가(기존 응답이 있으면 example만 병합)
        }
        return operation
    }
}
```

- OpenAPI `responses`는 상태 코드가 유일 키이므로, 같은 status의 코드가 여러 개면 **같은 응답 아래 example을 여러 개** 붙인다(그룹핑하지 않으면 하나가 덮여 사라진다).
- 이미 다른 곳에서 만든 응답(예: 200 정상 응답, 다른 커스터마이저 결과)이 있으면 새로 만들지 않고 example만 병합한다.
- `io.swagger...responses.ApiResponse`는 `dto.ApiResponse`와 이름이 충돌하므로 `import ... as OpenApiResponse`로 alias한다.
