package kr.dongchimi.core.product.importjob

data class ImportStepProgress(
    val step: ImportStep,
    val status: ImportStepStatus,
)
