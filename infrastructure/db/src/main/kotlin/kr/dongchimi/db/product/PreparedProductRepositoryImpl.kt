package kr.dongchimi.db.product

import kr.dongchimi.core.product.PreparedProductRepository
import org.springframework.stereotype.Repository

@Repository
class PreparedProductRepositoryImpl(
    private val preparedProductJpaRepository: PreparedProductJpaRepository,
) : PreparedProductRepository
