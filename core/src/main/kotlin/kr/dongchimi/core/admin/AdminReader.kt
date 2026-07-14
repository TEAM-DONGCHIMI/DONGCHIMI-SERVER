package kr.dongchimi.core.admin

import org.springframework.stereotype.Component

@Component
class AdminReader(
    private val adminRepository: AdminRepository,
) {
    fun existsByEmail(email: String): Boolean = adminRepository.existsByEmail(email)

    fun readByEmail(email: String): Admin? = adminRepository.findByEmail(email)

    fun readAllByIds(ids: Set<Long>): List<Admin> = adminRepository.findAllByIdIn(ids)

    /** 관리자는 수·요청이 적어 캐시 없이 DB로 직접 존재 검증한다. */
    fun existsById(adminId: Long): Boolean = adminRepository.existsById(adminId)
}
