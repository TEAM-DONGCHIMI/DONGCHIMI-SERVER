package kr.dongchimi.core.product.import

/**
 * 종료 상태별로 채워지는 필드가 다르다 — COMPLETED는 카운트만, FAILED는 errorCode만,
 * CANCELED는 둘 다 비운다(호출 시 result 자체를 null로 넘긴다).
 */
data class ImportJobResult(
    val totalCount: Int? = null,
    val successCount: Int? = null,
    val failCount: Int? = null,
    val errorCode: String? = null,
)
