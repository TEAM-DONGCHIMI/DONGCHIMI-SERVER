package kr.dongchimi.core.product.importjob

import kr.dongchimi.core.product.ProductCategory

interface ProductCategoryClassifier {
    /** 반환 Map에 없는 id는 분류 실패(null)로 취급한다. */
    suspend fun classify(items: List<ProductCategoryClassifyItem>): Map<Int, ProductCategory?>
}
