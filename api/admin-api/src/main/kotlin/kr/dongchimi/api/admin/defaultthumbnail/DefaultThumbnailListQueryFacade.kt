package kr.dongchimi.api.admin.defaultthumbnail

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.api.admin.defaultthumbnail.response.CreatedByResponse
import kr.dongchimi.api.admin.defaultthumbnail.response.DefaultThumbnailListItemResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.core.admin.Admin
import kr.dongchimi.core.admin.AdminService
import kr.dongchimi.core.admin.DefaultProductThumbnail
import kr.dongchimi.core.admin.DefaultProductThumbnailService
import kr.dongchimi.core.admin.DefaultThumbnailListCondition
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

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
                val admin = resolveAdmin(thumbnail, adminById)
                DefaultThumbnailListItemResponse(
                    defaultThumbnailId = thumbnail.id,
                    name = thumbnail.name,
                    thumbnailUrl = thumbnail.thumbnailUrl,
                    createdBy = admin?.let { CreatedByResponse(it.id, it.name, it.email) },
                    createdAt = thumbnail.createdAt,
                )
            }

        return CursorSliceResponse(content = content, hasNext = slice.hasNext, nextCursor = slice.nextCursor)
    }

    // createdBy는 목록 조회의 핵심 데이터가 아니라 부가 정보라, 등록자를 못 찾아도 요청 전체를
    // 실패시키지 않고 null로 내려준다. FK 제약이 없는 컬럼이라(coding-style.md 2-4절) 데이터
    // 정합성이 깨질 수 있음을 전제로 한 방어이며, 발생 시 원인 추적을 위해 경고만 남긴다.
    private fun resolveAdmin(
        thumbnail: DefaultProductThumbnail,
        adminById: Map<Long, Admin>,
    ): Admin? {
        val admin = adminById[thumbnail.createdBy]
        if (admin == null) {
            logger.warn { "썸네일(${thumbnail.id})의 등록자(${thumbnail.createdBy})를 찾을 수 없습니다." }
        }
        return admin
    }
}
