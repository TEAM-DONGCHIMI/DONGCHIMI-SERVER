package kr.dongchimi.core.owner

interface OwnerRepository {
    fun findById(id: Long): Owner?

    fun findByEmail(email: String): Owner?

    fun existsByEmail(email: String): Boolean

    fun save(owner: Owner): Owner
}
