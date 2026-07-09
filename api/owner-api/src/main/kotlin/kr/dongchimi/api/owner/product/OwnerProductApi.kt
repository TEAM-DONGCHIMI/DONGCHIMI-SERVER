package kr.dongchimi.api.owner.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.PageOffsetRequest
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.PreparedProductDraftSearchRequest
import kr.dongchimi.api.owner.product.request.ProductBulkDeleteRequest
import kr.dongchimi.api.owner.product.request.ProductDiscountPeriodUpdateRequest
import kr.dongchimi.api.owner.product.request.ProductResetRequest
import kr.dongchimi.api.owner.product.response.OwnerPreparedProductDraftListResponse
import kr.dongchimi.api.owner.product.response.OwnerProductDetailResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.product.ProductErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Product", description = "상품 API")
interface OwnerProductApi {
    @Operation(
        summary = "임시저장 상품 목록 조회",
        description = "점주가 임시 등록한 상품 목록을 검색어·카테고리로 필터링하고 페이지네이션하여 조회한다. 상단 카운트는 마트 전체 임시저장 기준이다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun getDrafts(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: PreparedProductDraftSearchRequest,
        pageOffsetRequest: PageOffsetRequest,
    ): ApiResponse<OwnerPreparedProductDraftListResponse>

    @Operation(
        summary = "상품 상세 조회",
        description = "점주가 수정할 때 각 상품 정보를 상세 조회한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ProductErrorCode::class)
    fun getDetail(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        @Parameter(description = "상품 ID") @PathVariable productId: Long,
    ): ApiResponse<OwnerProductDetailResponse>

    @Operation(
        summary = "상품 삭제",
        description = "점주가 등록된 상품을 삭제한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ProductErrorCode::class)
    fun delete(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        @Parameter(description = "상품 ID") @PathVariable productId: Long,
    ): ApiResponse<Unit>

    @Operation(
        summary = "상품 일괄 삭제",
        description = "점주가 등록된 상품을 여러 개 한번에 삭제한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ProductErrorCode::class)
    fun deleteAll(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: ProductBulkDeleteRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "상품 기간 일괄 수정",
        description = "점주가 여러 상품의 할인 기간을 한번에 수정한다. (오늘의 특가·기간 할인 모두 사용)",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ProductErrorCode::class)
    fun updateDiscountPeriod(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: ProductDiscountPeriodUpdateRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "상품 초기화",
        description = "점주가 등록한 상품들을 할인 유형별로 초기화(전부 삭제)한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun reset(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: ProductResetRequest,
    ): ApiResponse<Unit>
}
