package kr.dongchimi.api.owner.home.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.FlyerShareInfo

data class HomeFlyerResponse(
    @Schema(description = "전단 id")
    val flyerId: Long,
    @Schema(description = "공유용 URL slug")
    val slug: String,
    @Schema(description = "QR코드 이미지 (Base64 인코딩, 없으면 null)")
    val qrCode: String?,
) {
    constructor(flyerShareInfo: FlyerShareInfo) : this(
        flyerShareInfo.marketId,
        flyerShareInfo.slug,
        flyerShareInfo.qrCode,
    )
}
