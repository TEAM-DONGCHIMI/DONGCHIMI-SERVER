package kr.dongchimi.core.owner

interface OwnerRepository {
    fun findById(id: Long): Owner?

    fun save(owner: Owner): Owner
}
