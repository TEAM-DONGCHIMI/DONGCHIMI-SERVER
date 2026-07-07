package kr.dongchimi.core.owner

import kr.dongchimi.core.owner.exception.DuplicateEmailException
import org.springframework.stereotype.Service

@Service
class OwnerSignupService(
    private val ownerReader: OwnerReader,
    private val ownerAppender: OwnerAppender,
) {
    fun signup(command: OwnerSignupCommand): Owner {
        if (ownerReader.existsByEmail(command.email)) {
            throw DuplicateEmailException()
        }
        return ownerAppender.append(command)
    }
}
