package kr.dongchimi.api.admin.defaultthumbnail

import kr.dongchimi.api.admin.defaultthumbnail.response.CreatedByResponse
import kr.dongchimi.api.admin.defaultthumbnail.response.DefaultThumbnailListItemResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.core.admin.AdminService
import kr.dongchimi.core.admin.DefaultProductThumbnailService
import kr.dongchimi.core.admin.DefaultThumbnailListCondition
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DefaultThumbnailListQueryFacade(
    private val defaultProductThumbnailService: DefaultProductThumbnailService,
    private val adminService: AdminService,
) {
    @Transactional(readOnly = true)
    fun getList(condition: DefaultThumbnailListCondition): CursorSliceResponse<DefaultThumbnailListItemResponse> {
        val slice = defaultProductThumbnailService.getList(condition)

        val adminIds = slice.content.map { it.createdBy }.toSet()
        val adminById = adminService.findByIds(adminIds).associateBy { it.id }

        val content =
            slice.content.map { thumbnail ->
                val admin =
                    checkNotNull(adminById[thumbnail.createdBy]) {
                        "썸네일(${thumbnail.id})의 등록자(${thumbnail.createdBy})를 찾을 수 없습니다."
                    }
                DefaultThumbnailListItemResponse(
                    defaultThumbnailId = thumbnail.id,
                    name = thumbnail.name,
                    thumbnailUrl = thumbnail.thumbnailUrl,
                    createdBy = CreatedByResponse(admin.id, admin.name, admin.email),
                    createdAt = thumbnail.createdAt,
                )
            }

        return CursorSliceResponse(content = content, hasNext = slice.hasNext, nextCursor = slice.nextCursor)
    }
}
