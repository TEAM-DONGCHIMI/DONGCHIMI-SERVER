package kr.dongchimi.api.admin.defaultthumbnail.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.common.exception.InvalidInputException
import kr.dongchimi.api.core.common.exception.validate
import kr.dongchimi.core.admin.DefaultThumbnailBulkCreateCommand
import kr.dongchimi.core.admin.DefaultThumbnailCreateItem
import kr.dongchimi.core.product.toProductCategoryOrNull

data class DefaultThumbnailBulkCreateRequest(
    @Schema(description = "일괄 등록할 기본 이미지 목록")
    val thumbnails: List<DefaultThumbnailCreateItemRequest>,
) {
    fun toCommand(): DefaultThumbnailBulkCreateCommand {
        validate(thumbnails.isNotEmpty()) { "등록할 이미지가 없습니다." }

        return DefaultThumbnailBulkCreateCommand(items = thumbnails.map { it.toItem() })
    }
}

data class DefaultThumbnailCreateItemRequest(
    @Schema(description = "이름", example = "김치")
    val name: String,
    @Schema(description = "썸네일 이미지 URL", example = "https://cdn.example.com/test.png")
    val thumbnailUrl: String,
    @Schema(description = "카테고리", example = "VEGETABLE_FRUIT")
    val category: String,
) {
    fun toItem(): DefaultThumbnailCreateItem {
        validate(name.isNotBlank()) { "이미지 이름을 입력해 주세요." }
        validate(thumbnailUrl.isNotBlank()) { "이미지 URL을 입력해 주세요." }
        val category = category.toProductCategoryOrNull() ?: throw InvalidInputException("카테고리가 올바르지 않습니다.")

        return DefaultThumbnailCreateItem(name = name, thumbnailUrl = thumbnailUrl, category = category)
    }
}
