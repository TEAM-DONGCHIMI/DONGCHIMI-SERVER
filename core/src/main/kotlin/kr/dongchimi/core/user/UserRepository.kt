package kr.dongchimi.core.user

interface UserRepository {
    fun findById(id: Long): User?

    fun save(user: User): User
}
