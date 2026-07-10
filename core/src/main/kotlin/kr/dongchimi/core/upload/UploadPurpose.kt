package kr.dongchimi.core.upload

private val IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")
private val EXCEL_TYPES = setOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")

enum class UploadPurpose(
    val prefix: String,
    val allowedContentTypes: Set<String>,
    val maxSizeBytes: Long,
) {
    PRODUCT_THUMBNAIL("products/thumbnails", IMAGE_TYPES, 5 * 1024 * 1024L),
    DEFAULT_PRODUCT_THUMBNAIL("admin/default-thumbnails", IMAGE_TYPES, 5 * 1024 * 1024L),
    PRODUCT_IMPORT_EXCEL("products/imports", EXCEL_TYPES, 10 * 1024 * 1024L),
}
