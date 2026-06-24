package kr.dongchimi.api.common.dto

data class ApiResponse<T> private constructor(
    val success: Boolean,
    val code: String = "SUCCESS",
    val message: String,
    val data: T? = null,
) {

    companion object {
        private const val SUCCESS_MESSAGE = "요청에 성공했습니다."

        fun <T> success(data: T? = null): ApiResponse<T> {
            return ApiResponse(true, message = SUCCESS_MESSAGE, data = data)
        }

        fun <T> error(code: String, message: String): ApiResponse<T> {
            return ApiResponse(false, code, message)
        }
    }
}