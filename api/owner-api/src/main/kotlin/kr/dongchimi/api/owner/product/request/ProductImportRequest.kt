package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.product.ProductImportCommand

data class ProductImportRequest(
    @Schema(description = "S3에 업로드한 엑셀 파일 URL", example = "https://cdn.dongchimi.kr/products/imports/2026/07/x.xlsx")
    val excelFileUrl: String,
) {
    fun toCommand(): ProductImportCommand {
        validate(excelFileUrl.isNotBlank()) { "엑셀 파일 URL은 필수로 입력해 주세요." }

        return ProductImportCommand(excelFileUrl)
    }
}
