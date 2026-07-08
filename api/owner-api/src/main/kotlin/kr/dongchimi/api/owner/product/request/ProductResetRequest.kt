package kr.dongchimi.api.owner.product.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.DealType

data class ProductResetRequest(
    @Schema(description = "초기화할 할인 유형", example = "DAILY")
    val dealType: DealType,
)
