package kr.dongchimi.core.product.importjob

enum class ImportStep {
    FILE_UPLOAD,
    NAME_EXTRACTION,
    PRICE_EXTRACTION,
    CATEGORY_CLASSIFICATION,
    IMAGE_MATCHING,
}
