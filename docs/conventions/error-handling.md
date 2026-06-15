# Error Handling — 예외 처리 & Swagger 에러 문서화

> 이 문서는 ErrorCode/CustomException 체계, GlobalExceptionHandler, Swagger 에러 응답 문서화 방식을 다룬다.
> 새 에러 코드 추가, 예외 처리 로직 작업 시 참조한다.

---

## 1. 예외 처리

- `ErrorCode` 인터페이스를 정의한다 (`httpStatus`, `message`, `name`)
- 도메인별 enum으로 구현한다: `UserErrorCode`, `AuthErrorCode`

```kotlin
interface ErrorCode {
    val httpStatus: HttpStatus
    val message: String
    val name: String
}

enum class UserErrorCode(
    override val httpStatus: HttpStatus,
    override val message: String,
) : ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");

    override val name: String get() = this.name
}
```

- 예외 클래스 계층: `CustomException` → `AuthException`, `BusinessException`

```kotlin
open class CustomException(
    val errorCode: ErrorCode,
) : RuntimeException(errorCode.message)

class BusinessException(errorCode: ErrorCode) : CustomException(errorCode)
class AuthException(errorCode: ErrorCode) : CustomException(errorCode)
```

- `GlobalExceptionHandler`에서 전역 처리한다

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<ErrorResponse> =
        ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(ErrorResponse(message = e.errorCode.message))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val message = e.bindingResult.fieldErrors.firstOrNull()?.defaultMessage ?: "잘못된 요청입니다."
        return ResponseEntity.badRequest().body(ErrorResponse(message = message))
    }
}
```

---

## 2. Swagger 에러 문서화

- 커스텀 어노테이션 `@ApiErrorCode`를 사용하여 에러 응답을 문서화한다

```kotlin
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorCode(
    val code: KClass<out ErrorCode>,
    val names: String,
)
```

- `ApiErrorCodeOperationCustomizer`가 OpenAPI 응답 스키마를 자동으로 생성한다

```kotlin
@Component
class ApiErrorCodeOperationCustomizer : OperationCustomizer {
    override fun customize(operation: Operation, handlerMethod: HandlerMethod): Operation {
        handlerMethod.getMethodAnnotation(ApiErrorCode::class.java)?.let { annotation ->
            // annotation.code, annotation.names 기반으로 OpenAPI 응답 스키마 추가
        }
        return operation
    }
}
```
