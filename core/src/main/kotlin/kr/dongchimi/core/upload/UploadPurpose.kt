package kr.dongchimi.core.upload

private val IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")

enum class UploadPurpose(
    val prefix: String,
    val allowedContentTypes: Set<String>,
    val maxSizeBytes: Long,
) {
    PRODUCT_THUMBNAIL("products/thumbnails", IMAGE_TYPES, 5 * 1024 * 1024L),
    DEFAULT_PRODUCT_THUMBNAIL("admin/default-thumbnails", IMAGE_TYPES, 5 * 1024 * 1024L),
}
