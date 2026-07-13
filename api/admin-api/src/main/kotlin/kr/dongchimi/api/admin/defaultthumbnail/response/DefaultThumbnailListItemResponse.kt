package kr.dongchimi.api.admin.defaultthumbnail.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class DefaultThumbnailListItemResponse(
    @Schema(description = "기본 썸네일 id")
    val defaultThumbnailId: Long,
    @Schema(description = "이름")
    val name: String,
    @Schema(description = "썸네일 이미지 URL")
    val thumbnailUrl: String,
    @Schema(description = "등록자 (등록자 정보를 찾을 수 없으면 null)")
    val createdBy: CreatedByResponse?,
    @Schema(description = "등록 일시")
    val createdAt: LocalDateTime,
)
