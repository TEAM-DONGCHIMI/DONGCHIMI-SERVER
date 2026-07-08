package kr.dongchimi.core.owner

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Service

@Service
class OwnerSignupService(
    private val ownerReader: OwnerReader,
    private val ownerAppender: OwnerAppender,
) {
    fun signup(command: OwnerSignupCommand): Owner {
        if (ownerReader.existsByEmail(command.email)) {
            throw CoreException(OwnerErrorCode.DUPLICATE_EMAIL)
        }
        return ownerAppender.append(command)
    }
}
