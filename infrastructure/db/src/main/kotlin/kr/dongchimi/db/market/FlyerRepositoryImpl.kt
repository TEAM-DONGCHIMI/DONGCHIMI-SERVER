package kr.dongchimi.db.market

import kr.dongchimi.core.market.Flyer
import kr.dongchimi.core.market.FlyerRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class FlyerRepositoryImpl(
    private val flyerJpaRepository: FlyerJpaRepository,
) : FlyerRepository {
    override fun findById(id: Long): Flyer? = flyerJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(flyer: Flyer): Flyer = flyerJpaRepository.save(FlyerJpaEntity(flyer)).toDomain()
}
