package kr.dongchimi.core.admin

import org.springframework.stereotype.Component

@Component
class AdminReader(
    private val adminRepository: AdminRepository,
) {
    fun existsByEmail(email: String): Boolean = adminRepository.existsByEmail(email)

    fun readByEmail(email: String): Admin? = adminRepository.findByEmail(email)

    fun readAllByIds(ids: Set<Long>): List<Admin> = adminRepository.findAllByIdIn(ids)
}
