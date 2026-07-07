package kr.dongchimi.core.monitoring

data class ErrorContext(
    val throwable: Throwable,
    val requestId: String?,
    val userId: String?,
    val requestMethod: String?,
    val requestUri: String?,
    val requestBody: String?,
)
