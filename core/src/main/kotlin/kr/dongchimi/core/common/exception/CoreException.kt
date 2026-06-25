package kr.dongchimi.core.common.exception

open class CoreException(
    val errorCode: ErrorCode,
    message: String = errorCode.message,
    formatArgs: List<Any> = emptyList()
): RuntimeException(
    if (formatArgs.isEmpty()) {
        message
    } else {
        message.format(*formatArgs.toTypedArray())
    }
)