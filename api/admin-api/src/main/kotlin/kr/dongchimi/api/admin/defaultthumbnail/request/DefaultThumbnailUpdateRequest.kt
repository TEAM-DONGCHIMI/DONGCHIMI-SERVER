package kr.dongchimi.api.admin.defaultthumbnail.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.admin.DefaultThumbnailUpdateCommand
import kr.dongchimi.core.product.toProductCategoryOrNull

data class DefaultThumbnailUpdateRequest(
    @Schema(description = "이름", example = "김치")
    val name: String,
    @Schema(description = "썸네일 이미지 URL", example = "https://cdn.example.com/test.png")
    val thumbnailUrl: String,
    @Schema(description = "카테고리", example = "VEGETABLE_FRUIT")
    val category: String,
) {
    fun toCommand(id: Long): DefaultThumbnailUpdateCommand {
        validate(name.isNotBlank()) { "이미지 이름을 입력해 주세요." }
        validate(thumbnailUrl.isNotBlank()) { "이미지 URL을 입력해 주세요." }
        val category = category.toProductCategoryOrNull() ?: throw InvalidInputException("카테고리가 올바르지 않습니다.")

        return DefaultThumbnailUpdateCommand(id = id, name = name, thumbnailUrl = thumbnailUrl, category = category)
    }
}
