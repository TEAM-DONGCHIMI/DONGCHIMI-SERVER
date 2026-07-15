package kr.dongchimi.core.owner

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class OwnerValidator(
    private val ownerReader: OwnerReader,
) {
    /** 사장님 계정이 존재하지 않으면 401을 던진다. */
    fun validate(ownerId: Long) {
        if (!ownerReader.existsById(ownerId)) throw CoreException(OwnerErrorCode.OWNER_NOT_FOUND)
    }
}
