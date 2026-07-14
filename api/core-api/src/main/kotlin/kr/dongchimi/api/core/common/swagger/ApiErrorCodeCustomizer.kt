package kr.dongchimi.api.core.common.swagger

import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.responses.ApiResponses
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.core.common.exception.ErrorCode
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import io.swagger.v3.oas.models.responses.ApiResponse as OpenApiResponse

@Component
class ApiErrorCodeCustomizer : OperationCustomizer {
    override fun customize(
        operation: Operation,
        handlerMethod: HandlerMethod,
    ): Operation {
        val annotations = handlerMethod.method.getAnnotationsByType(ApiErrorCode::class.java)
        if (annotations.isEmpty()) return operation

        operation.responses = operation.responses ?: ApiResponses()

        val errorCodes: List<ErrorCode> = annotations.flatMap { it.resolve() }

        errorCodes.groupBy { it.status }.forEach { (status, codes) ->
            val mediaType = MediaType()
            codes.forEach { code ->
                mediaType.addExamples(
                    code.name,
                    Example()
                        .summary(code.name)
                        .value(ApiResponse.error<Unit>(code)),
                )
            }

            val statusKey = status.toString()
            val existing = operation.responses[statusKey]
            if (existing != null) {
                val existingContent = existing.content?.get("application/json")
                if (existingContent != null) {
                    existingContent.examples =
                        (existingContent.examples ?: mutableMapOf()).apply {
                            putAll(mediaType.examples)
                        }
                } else {
                    existing.content(Content().addMediaType("application/json", mediaType))
                }
            } else {
                operation.responses.addApiResponse(
                    statusKey,
                    OpenApiResponse()
                        .description("에러 응답")
                        .content(Content().addMediaType("application/json", mediaType)),
                )
            }
        }
        return operation
    }

    private fun ApiErrorCode.resolve(): List<ErrorCode> {
        val constants =
            type.java.enumConstants
                ?: error("@ApiErrorCode의 type(${type.simpleName})은 enum 이어야 합니다")
        return codes.map { code ->
            constants.firstOrNull { it.name == code }
                ?: error("@ApiErrorCode: ${type.simpleName}에 '$code' 코드가 존재하지 않습니다")
        }
    }
}
