package kr.dongchimi.db.admin

import kr.dongchimi.core.admin.Admin
import kr.dongchimi.core.admin.AdminRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class AdminRepositoryImpl(
    private val adminJpaRepository: AdminJpaRepository,
) : AdminRepository {
    override fun findById(id: Long): Admin? = adminJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(admin: Admin): Admin = adminJpaRepository.save(AdminJpaEntity(admin)).toDomain()
}
