package kr.dongchimi.core.product.importjob

import kr.dongchimi.core.product.ProductCategory

/**
 * 이미지 매칭 배치 호출의 항목. [id]는 원본 행 인덱스다. [category]는 이 항목이 어느 category 후보에서
 * 매칭돼야 하는지를 나타낸다 — 한 청크에 여러 category가 섞일 수 있어 항목마다 개별로 지닌다.
 */
data class ProductImageMatchItem(
    val id: Int,
    val productName: String,
    val category: ProductCategory,
)
