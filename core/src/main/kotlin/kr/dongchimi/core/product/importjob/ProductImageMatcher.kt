package kr.dongchimi.core.product.importjob

interface ProductImageMatcher {
    /**
     * 한 청크에 여러 category가 섞일 수 있고, 각 항목은 자신의 [ProductImageMatchItem.category] 후보 중에서 매칭된다.
     * 반환 Map에 없는 id는 매칭 실패(null)로 취급한다.
     */
    suspend fun match(items: List<ProductImageMatchItem>): Map<Int, String?>
}
