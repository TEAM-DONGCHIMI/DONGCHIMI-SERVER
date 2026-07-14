package kr.dongchimi.api.owner.auth.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.owner.Owner

data class OwnerLoginResponse(
    @Schema(description = "액세스 토큰 (JWT)")
    val accessToken: String,
    @Schema(description = "점주 id")
    val ownerId: Long,
    @Schema(description = "점주 이메일")
    val email: String,
    @Schema(description = "마트 id (미등록 시 null)")
    val marketId: Long?,
    @Schema(description = "마트 이름 (미등록 시 null)")
    val marketName: String?,
    @Schema(description = "마트 이미지 URL (미등록 시 null)")
    val marketThumbnailUrl: String?,
) {
    constructor(accessToken: String, owner: Owner, market: Market?) : this(
        accessToken = accessToken,
        ownerId = owner.id,
        email = owner.email,
        marketId = market?.id,
        marketName = market?.info?.name,
        marketThumbnailUrl = market?.info?.thumbnailUrl,
    )
}
