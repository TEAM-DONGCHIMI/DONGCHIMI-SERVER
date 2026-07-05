package kr.dongchimi.core.user

interface UserRepository {
    fun findById(id: Long): User?

    fun findBySocialAccount(account: SocialAccount): User?

    fun save(user: User): User
}
