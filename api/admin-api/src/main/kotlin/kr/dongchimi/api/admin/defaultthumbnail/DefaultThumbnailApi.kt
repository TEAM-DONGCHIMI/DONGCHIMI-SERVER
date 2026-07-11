package kr.dongchimi.api.admin.defaultthumbnail

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.admin.AdminApiUser
import kr.dongchimi.api.admin.defaultthumbnail.request.DefaultThumbnailBulkCreateRequest
import kr.dongchimi.api.admin.defaultthumbnail.request.DefaultThumbnailListRequest
import kr.dongchimi.api.admin.defaultthumbnail.response.DefaultThumbnailListItemResponse
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.core.admin.DefaultProductThumbnailErrorCode
import kr.dongchimi.core.common.exception.CommonErrorCode

@Tag(name = "Default Thumbnail", description = "기본 상품 썸네일 관리 API")
interface DefaultThumbnailApi {
    @Operation(
        summary = "기본 썸네일 목록 조회",
        description = "상품 등록 시 자동 매칭에 쓰이는 기본 이미지 목록을 커서 기반으로 조회한다. 이름 검색과 최신순/이름순 정렬을 지원한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class)
    fun getList(
        @Parameter(hidden = true) apiUser: AdminApiUser,
        request: DefaultThumbnailListRequest,
    ): ApiResponse<CursorSliceResponse<DefaultThumbnailListItemResponse>>

    @Operation(
        summary = "기본 썸네일 일괄 등록",
        description = "상품 등록 시 자동 매칭에 쓰이는 기본 이미지를 여러 건 한 번에 등록한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, DefaultProductThumbnailErrorCode::class)
    fun bulkCreate(
        @Parameter(hidden = true) apiUser: AdminApiUser,
        request: DefaultThumbnailBulkCreateRequest,
    ): ApiResponse<Unit>
}
