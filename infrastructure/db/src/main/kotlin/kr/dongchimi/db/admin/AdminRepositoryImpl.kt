package kr.dongchimi.db.admin

import kr.dongchimi.core.admin.Admin
import kr.dongchimi.core.admin.AdminErrorCode
import kr.dongchimi.core.admin.AdminRepository
import kr.dongchimi.core.common.exception.CoreException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AdminRepositoryImpl(
    private val adminJpaRepository: AdminJpaRepository,
) : AdminRepository {
    override fun findById(id: Long): Admin? = adminJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun findByEmail(email: String): Admin? = adminJpaRepository.findByEmail(email)?.toDomain()

    override fun existsByEmail(email: String): Boolean = adminJpaRepository.existsByEmail(email)

    override fun existsById(id: Long): Boolean = adminJpaRepository.existsById(id)

    override fun findAllByIdIn(ids: Set<Long>): List<Admin> = adminJpaRepository.findAllById(ids).map { it.toDomain() }

    override fun save(admin: Admin): Admin =
        try {
            adminJpaRepository.save(AdminJpaEntity(admin)).toDomain()
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isAdminEmailUniqueViolation()) {
                throw CoreException(AdminErrorCode.DUPLICATE_EMAIL)
            }
            throw exception
        }

    private fun DataIntegrityViolationException.isAdminEmailUniqueViolation(): Boolean =
        mostSpecificCause.message?.contains(ADMIN_EMAIL_UNIQUE_INDEX) == true

    companion object {
        private const val ADMIN_EMAIL_UNIQUE_INDEX = "uq_admins_email"
    }
}
