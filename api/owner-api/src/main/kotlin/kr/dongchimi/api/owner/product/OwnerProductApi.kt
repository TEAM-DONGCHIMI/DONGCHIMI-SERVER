package kr.dongchimi.api.owner.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.api.core.common.dto.PageOffsetRequest
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.DailyProductRegisterRequest
import kr.dongchimi.api.owner.product.request.OwnerProductListRequest
import kr.dongchimi.api.owner.product.request.PreparedProductDraftSaveRequest
import kr.dongchimi.api.owner.product.request.PreparedProductDraftSearchRequest
import kr.dongchimi.api.owner.product.request.ProductBulkDeleteRequest
import kr.dongchimi.api.owner.product.request.ProductDiscountPeriodUpdateRequest
import kr.dongchimi.api.owner.product.request.ProductResetRequest
import kr.dongchimi.api.owner.product.request.ProductSearchRequest
import kr.dongchimi.api.owner.product.request.ProductUpdateRequest
import kr.dongchimi.api.owner.product.response.OwnerPreparedProductDraftListResponse
import kr.dongchimi.api.owner.product.response.OwnerProductDetailResponse
import kr.dongchimi.api.owner.product.response.OwnerProductListItemResponse
import kr.dongchimi.api.owner.product.response.ProductSearchResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.product.PreparedProductErrorCode
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
        summary = "상품 임시저장",
        description =
            "점주가 편집 중인 임시저장 상품을 저장한다. 요청에 담긴 상품만 갱신하고 나머지 임시저장 상품은 건드리지 않는다. " +
                "필수값(이미지·카테고리·상품명·판매가격·할인기간)이 모두 채워지면 SUCCESS, 하나라도 비면 FAIL로 상태가 갱신된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, PreparedProductErrorCode::class)
    fun saveDrafts(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: PreparedProductDraftSaveRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "상품 최종 저장",
        description =
            "임시저장 상품을 실제 상품으로 일괄 등록한다. 마트의 임시저장 상품 중 하나라도 FAIL이면 " +
                "아무것도 등록되지 않는다(all-or-nothing). 등록된 임시저장 상품은 삭제된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, PreparedProductErrorCode::class)
    fun confirmDrafts(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
    ): ApiResponse<Unit>

    @Operation(
        summary = "오늘의 특가 등록",
        description = "점주가 오늘의 특가(DAILY) 상품을 단건 등록한다. 기간은 오늘을 포함해야 하며, 썸네일 미입력 시 null로 저장한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ProductErrorCode::class)
    fun registerDailyProduct(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: DailyProductRegisterRequest,
    ): ApiResponse<Unit>

    @Operation(
        summary = "상품 목록 조회",
        description =
            "점주가 등록한 상품을 type(PERIODIC/DAILY)별로, 오늘 할인 진행 중인 것만 커서 무한스크롤로 조회한다. " +
                "정렬은 CATEGORY(기본)/LATEST/VIEW_COUNT, category(카테고리) 필터를 지원한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun getProducts(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: OwnerProductListRequest,
    ): ApiResponse<CursorSliceResponse<OwnerProductListItemResponse>>

    @Operation(
        summary = "상품 검색",
        description = "점주가 상품명으로 마트 내 오늘 할인 진행 중인 상품을 검색한다. 대소문자를 구분하지 않으며, 초성만 입력하면 초성 일치로 검색한다. 최근 등록순으로 최대 size건 반환한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class)
    fun search(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: ProductSearchRequest,
    ): ApiResponse<ProductSearchResponse>

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
        summary = "상품 수정",
        description =
            "점주가 등록한 상품의 판매 정보를 수정한다. 요청 type이 상품의 기존 판매 유형과 일치해야 하며, " +
                "DAILY는 정가·할인가를 모두 받고 기간이 오늘을 포함해야 한다. PERIODIC은 판매가만 받는다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ProductErrorCode::class)
    fun updateProduct(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        @Parameter(description = "상품 ID") @PathVariable productId: Long,
        request: ProductUpdateRequest,
    ): ApiResponse<Unit>

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
