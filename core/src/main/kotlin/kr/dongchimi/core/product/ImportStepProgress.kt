package kr.dongchimi.core.product

data class ImportStepProgress(
    val step: ImportStep,
    val status: ImportStepStatus,
)
