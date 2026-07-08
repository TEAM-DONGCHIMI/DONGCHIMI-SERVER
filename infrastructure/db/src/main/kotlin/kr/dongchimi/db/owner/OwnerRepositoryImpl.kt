package kr.dongchimi.db.owner

import kr.dongchimi.core.owner.Owner
import kr.dongchimi.core.owner.OwnerRepository
import kr.dongchimi.core.owner.exception.DuplicateEmailException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class OwnerRepositoryImpl(
    private val ownerJpaRepository: OwnerJpaRepository,
) : OwnerRepository {
    override fun findById(id: Long): Owner? = ownerJpaRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()

    override fun findByEmail(email: String): Owner? = ownerJpaRepository.findByEmailAndDeletedAtIsNull(email)?.toDomain()

    override fun existsByEmail(email: String): Boolean = ownerJpaRepository.existsByEmailAndDeletedAtIsNull(email)

    override fun save(owner: Owner): Owner =
        try {
            ownerJpaRepository.save(OwnerJpaEntity(owner)).toDomain()
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isOwnerEmailUniqueViolation()) {
                throw DuplicateEmailException(cause = exception)
            }
            throw exception
        }

    private fun DataIntegrityViolationException.isOwnerEmailUniqueViolation(): Boolean =
        mostSpecificCause.message?.contains(OWNER_EMAIL_UNIQUE_INDEX) == true

    companion object {
        private const val OWNER_EMAIL_UNIQUE_INDEX = "uq_owners_email_active"
    }
}
