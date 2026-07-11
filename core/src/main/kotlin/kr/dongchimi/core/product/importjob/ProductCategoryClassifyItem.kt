package kr.dongchimi.core.product.importjob

/** 카테고리 분류 배치 호출의 항목. [id]는 원본 행 인덱스로, 응답을 요청 항목에 되짚는 데 쓴다. */
data class ProductCategoryClassifyItem(
    val id: Int,
    val productName: String,
)
