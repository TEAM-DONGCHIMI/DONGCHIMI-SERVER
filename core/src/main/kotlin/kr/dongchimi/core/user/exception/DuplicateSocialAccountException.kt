package kr.dongchimi.core.user.exception

import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.UserErrorCode

class DuplicateSocialAccountException(
    cause: Throwable? = null,
) : CoreException(UserErrorCode.DUPLICATE_SOCIAL_ACCOUNT) {
    init {
        cause?.let(::initCause)
    }
}
