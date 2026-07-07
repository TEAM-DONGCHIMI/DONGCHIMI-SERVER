package kr.dongchimi.api.owner.auth.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.owner.OwnerLoginResult

data class OwnerLoginResponse(
    @Schema(description = "액세스 토큰 (JWT)")
    val accessToken: String,
    @Schema(description = "사장님 id")
    val ownerId: Long,
    @Schema(description = "사장님 이메일")
    val email: String,
    @Schema(description = "마트 id (미등록 시 null)")
    val marketId: Long?,
    @Schema(description = "마트 이름 (미등록 시 null)")
    val marketName: String?,
    @Schema(description = "마트 이미지 URL (미등록 시 null)")
    val marketThumbnailUrl: String?,
) {
    companion object {
        fun from(result: OwnerLoginResult): OwnerLoginResponse =
            OwnerLoginResponse(
                accessToken = result.tokens.accessToken,
                ownerId = result.owner.id,
                email = result.owner.email,
                marketId = result.market?.id,
                marketName = result.market?.name,
                marketThumbnailUrl = result.market?.thumbnailUrl,
            )
    }
}
