package kr.dongchimi.core.product

interface ProductCategoryClassifier {
    /** null이면 매칭 실패다. */
    suspend fun classify(productName: String): ProductCategory?
}
