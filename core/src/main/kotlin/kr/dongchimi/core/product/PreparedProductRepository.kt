package kr.dongchimi.core.product

interface PreparedProductRepository {
    fun findById(id: Long): PreparedProduct?

    fun save(preparedProduct: PreparedProduct): PreparedProduct
}
