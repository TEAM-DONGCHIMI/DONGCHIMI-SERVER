package kr.dongchimi.api.admin.defaultthumbnail

import kr.dongchimi.api.admin.AdminApiUser
import kr.dongchimi.api.admin.defaultthumbnail.request.DefaultThumbnailBulkCreateRequest
import kr.dongchimi.api.admin.defaultthumbnail.request.DefaultThumbnailListRequest
import kr.dongchimi.api.admin.defaultthumbnail.response.DefaultThumbnailListItemResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.core.admin.DefaultProductThumbnailService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/default-thumbnails")
class DefaultThumbnailController(
    private val defaultThumbnailListQueryFacade: DefaultThumbnailListQueryFacade,
    private val defaultProductThumbnailService: DefaultProductThumbnailService,
) : DefaultThumbnailApi {
    @GetMapping
    override fun getList(
        apiUser: AdminApiUser,
        request: DefaultThumbnailListRequest,
    ): ApiResponse<CursorSliceResponse<DefaultThumbnailListItemResponse>> =
        ApiResponse.success(defaultThumbnailListQueryFacade.getList(request.toCondition()))

    @PostMapping
    override fun bulkCreate(
        apiUser: AdminApiUser,
        @RequestBody request: DefaultThumbnailBulkCreateRequest,
    ): ApiResponse<Unit> {
        defaultProductThumbnailService.bulkCreate(request.toCommand(), apiUser.userId)
        return ApiResponse.success()
    }
}
