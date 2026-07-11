package kr.dongchimi.api.admin.defaultthumbnail

import kr.dongchimi.api.admin.AdminApiUser
import kr.dongchimi.api.admin.defaultthumbnail.request.DefaultThumbnailListRequest
import kr.dongchimi.api.admin.defaultthumbnail.response.DefaultThumbnailListItemResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/admin/default-thumbnails")
class DefaultThumbnailController(
    private val defaultThumbnailListQueryFacade: DefaultThumbnailListQueryFacade,
) : DefaultThumbnailApi {
    @GetMapping
    override fun getList(
        apiUser: AdminApiUser,
        request: DefaultThumbnailListRequest,
    ): ApiResponse<CursorSliceResponse<DefaultThumbnailListItemResponse>> =
        ApiResponse.success(defaultThumbnailListQueryFacade.getList(request.toCondition()))
}
