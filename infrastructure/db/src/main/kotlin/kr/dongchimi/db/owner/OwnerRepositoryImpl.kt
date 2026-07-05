package kr.dongchimi.db.owner

import kr.dongchimi.core.owner.Owner
import kr.dongchimi.core.owner.OwnerRepository
import org.springframework.stereotype.Repository

@Repository
class OwnerRepositoryImpl(
    private val ownerJpaRepository: OwnerJpaRepository,
) : OwnerRepository {
    override fun findById(id: Long): Owner? = ownerJpaRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()

    override fun save(owner: Owner): Owner = ownerJpaRepository.save(OwnerJpaEntity(owner)).toDomain()
}
