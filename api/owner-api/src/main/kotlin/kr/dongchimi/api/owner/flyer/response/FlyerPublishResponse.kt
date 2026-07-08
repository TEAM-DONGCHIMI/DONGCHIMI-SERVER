package kr.dongchimi.api.owner.flyer.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.FlyerPublish

data class FlyerPublishResponse(
    @Schema(description = "전단 공유 페이지 slug")
    val slug: String,
) {
    constructor(flyerPublish: FlyerPublish) : this(flyerPublish.slug)
}
