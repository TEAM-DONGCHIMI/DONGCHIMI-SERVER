package kr.dongchimi.db.product

interface PreparedProductDraftCountProjection {
    val totalCount: Long
    val successCount: Long
    val failCount: Long
}
