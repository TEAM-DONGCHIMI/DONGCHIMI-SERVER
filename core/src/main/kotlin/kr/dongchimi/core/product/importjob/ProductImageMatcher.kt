package kr.dongchimi.core.product.importjob

import kr.dongchimi.core.product.ProductCategory

interface ProductImageMatcher {
    /** null이면 매칭 실패다. */
    suspend fun match(
        productName: String,
        category: ProductCategory?,
    ): String?
}
