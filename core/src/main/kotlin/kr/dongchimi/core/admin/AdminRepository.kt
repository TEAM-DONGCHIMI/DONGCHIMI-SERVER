package kr.dongchimi.core.admin

interface AdminRepository {
    fun findById(id: Long): Admin?

    fun save(admin: Admin): Admin
}
