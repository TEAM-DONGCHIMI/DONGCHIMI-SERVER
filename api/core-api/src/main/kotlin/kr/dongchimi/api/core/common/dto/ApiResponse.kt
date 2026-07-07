package kr.dongchimi.api.core.common.dto

import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.common.exception.ErrorCode

data class ApiResponse<T> private constructor(
    val success: Boolean,
    val code: String = "SUCCESS",
    val message: String,
    val data: T? = null,
) {
    companion object {
        private const val SUCCESS_MESSAGE = "요청에 성공했습니다."

        fun <T> success(data: T? = null): ApiResponse<T> = ApiResponse(true, message = SUCCESS_MESSAGE, data = data)

        fun <T> error(exception: CoreException): ApiResponse<T> {
            val errorCode = exception.errorCode

            return ApiResponse(false, errorCode.name, exception.message ?: errorCode.message)
        }

        fun <T> error(errorCode: ErrorCode): ApiResponse<T> = ApiResponse(false, errorCode.name, errorCode.message)
    }
}
