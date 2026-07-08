package kr.dongchimi.api.owner.flyer.response

import io.swagger.v3.oas.annotations.media.Schema

data class FlyerPublishResponse(
    @Schema(description = "전단 공유 페이지 slug")
    val slug: String,
)
