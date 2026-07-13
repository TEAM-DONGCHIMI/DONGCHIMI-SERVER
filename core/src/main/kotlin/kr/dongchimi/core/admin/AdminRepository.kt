package kr.dongchimi.core.admin

interface AdminRepository {
    fun findById(id: Long): Admin?

    fun findByEmail(email: String): Admin?

    fun existsByEmail(email: String): Boolean

    fun findAllByIdIn(ids: Set<Long>): List<Admin>

    fun save(admin: Admin): Admin
}
