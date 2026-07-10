package kr.dongchimi.core.product.import

data class ImportStepProgress(
    val step: ImportStep,
    val status: ImportStepStatus,
)
