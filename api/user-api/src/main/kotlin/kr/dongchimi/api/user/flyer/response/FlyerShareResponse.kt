package kr.dongchimi.api.user.flyer.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.FlyerShareInfo

data class FlyerShareResponse(
    @Schema(description = "마트 id")
    val marketId: Long,
    @Schema(description = "마트명")
    val marketName: String,
    @Schema(description = "공유용 URL slug")
    val slug: String,
    @Schema(description = "QR코드 이미지 (Base64 인코딩)")
    val qrCode: String,
) {
    constructor(info: FlyerShareInfo) : this(info.marketId, info.marketName, info.slug, info.qrCode)
}
