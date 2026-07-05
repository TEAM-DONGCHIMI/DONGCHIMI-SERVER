package kr.dongchimi.api.core.exception

import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.common.exception.CoreException

class InvalidInputException(
    message: String,
) : CoreException(CommonErrorCode.INVALID_INPUT, message)

fun validate(
    condition: Boolean,
    errorMessage: () -> String,
) {
    if (!condition) throw InvalidInputException(errorMessage())
}
