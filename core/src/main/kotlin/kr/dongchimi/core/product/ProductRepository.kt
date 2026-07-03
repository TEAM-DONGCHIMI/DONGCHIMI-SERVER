package kr.dongchimi.core.product

interface ProductRepository {
    fun findById(id: Long): Product?

    fun save(product: Product): Product
}
