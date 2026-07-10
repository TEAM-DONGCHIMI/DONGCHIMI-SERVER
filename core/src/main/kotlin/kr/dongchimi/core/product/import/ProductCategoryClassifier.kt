package kr.dongchimi.core.product.import

import kr.dongchimi.core.product.ProductCategory

interface ProductCategoryClassifier {
    /** null이면 매칭 실패다. */
    suspend fun classify(productName: String): ProductCategory?
}
