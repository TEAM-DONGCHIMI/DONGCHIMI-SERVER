package kr.dongchimi.db.product

import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class PreparedProductRepositoryImpl(
    private val preparedProductJpaRepository: PreparedProductJpaRepository,
) : PreparedProductRepository {
    override fun findById(id: Long): PreparedProduct? = preparedProductJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(preparedProduct: PreparedProduct): PreparedProduct =
        preparedProductJpaRepository.save(PreparedProductJpaEntity(preparedProduct)).toDomain()
}
