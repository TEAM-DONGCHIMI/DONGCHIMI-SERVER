package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.DealType

data class ProductResetRequest(
    @Schema(description = "초기화할 할인 유형", example = "DAILY")
    val dealType: DealType,
    @Schema(description = "할인 기간 중이어도 삭제할지 여부", example = "false")
    val forceDelete: Boolean = false,
)
