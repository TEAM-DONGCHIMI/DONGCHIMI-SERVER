package kr.dongchimi.core.upload

private val IMAGE_TYPES = setOf("image/jpeg", "image/png", "image/webp")

enum class UploadPurpose(
    val prefix: String,
    val allowedContentTypes: Set<String>,
) {
    PRODUCT_THUMBNAIL("products/thumbnails", IMAGE_TYPES),
    DEFAULT_PRODUCT_THUMBNAIL("admin/default-thumbnails", IMAGE_TYPES),
}
