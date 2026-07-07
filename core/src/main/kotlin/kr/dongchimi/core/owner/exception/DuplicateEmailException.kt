package kr.dongchimi.core.owner.exception

import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.owner.OwnerErrorCode

class DuplicateEmailException(
    cause: Throwable? = null,
) : CoreException(OwnerErrorCode.DUPLICATE_EMAIL) {
    init {
        cause?.let(::initCause)
    }
}
