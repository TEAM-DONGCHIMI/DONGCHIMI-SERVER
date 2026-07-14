package kr.dongchimi.core.user

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class UserValidator(
    private val userReader: UserReader,
) {
    /** 유저가 존재하지 않으면 401을 던진다. */
    fun validate(userId: Long) {
        if (!userReader.existsById(userId)) throw CoreException(UserErrorCode.USER_NOT_FOUND)
    }
}
