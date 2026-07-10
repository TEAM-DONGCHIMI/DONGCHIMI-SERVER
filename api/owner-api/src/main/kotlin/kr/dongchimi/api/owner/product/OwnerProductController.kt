package kr.dongchimi.api.owner.product

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.api.core.common.dto.PageOffsetRequest
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.DailyProductRegisterRequest
import kr.dongchimi.api.owner.product.request.OwnerProductListRequest
import kr.dongchimi.api.owner.product.request.PreparedProductDraftSaveRequest
import kr.dongchimi.api.owner.product.request.PreparedProductDraftSearchRequest
import kr.dongchimi.api.owner.product.request.ProductBulkDeleteRequest
import kr.dongchimi.api.owner.product.request.ProductDiscountPeriodUpdateRequest
import kr.dongchimi.api.owner.product.request.ProductResetRequest
import kr.dongchimi.api.owner.product.request.ProductUpdateRequest
import kr.dongchimi.api.owner.product.response.OwnerPreparedProductDraftListResponse
import kr.dongchimi.api.owner.product.response.OwnerProductDetailResponse
import kr.dongchimi.api.owner.product.response.OwnerProductListItemResponse
import kr.dongchimi.core.product.PreparedProductService
import kr.dongchimi.core.product.ProductService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/v1/owners/markets/{marketId}/products")
class OwnerProductController(
    private val productService: ProductService,
    private val preparedProductService: PreparedProductService,
) : OwnerProductApi {
    @GetMapping("/draft")
    override fun getDrafts(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @ModelAttribute request: PreparedProductDraftSearchRequest,
        @ModelAttribute pageOffsetRequest: PageOffsetRequest,
    ): ApiResponse<OwnerPreparedProductDraftListResponse> {
        val condition = request.toSearchCondition()
        val pageOffset = pageOffsetRequest.toPageOffset()
        val counts = preparedProductService.getDraftCounts(apiUser.userId, marketId)
        val preparedProducts = preparedProductService.getDrafts(apiUser.userId, marketId, condition, pageOffset)

        return ApiResponse.success(OwnerPreparedProductDraftListResponse(counts, preparedProducts))
    }

    @PutMapping("/draft")
    override fun saveDrafts(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: PreparedProductDraftSaveRequest,
    ): ApiResponse<Unit> {
        preparedProductService.saveDrafts(apiUser.userId, marketId, request.toCommands())

        return ApiResponse.success()
    }

    @PostMapping
    override fun confirmDrafts(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<Unit> {
        preparedProductService.confirmDrafts(apiUser.userId, marketId)

        return ApiResponse.success()
    }

    @PostMapping("/daily")
    override fun registerDailyProduct(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: DailyProductRegisterRequest,
    ): ApiResponse<Unit> {
        productService.registerDailyProduct(apiUser.userId, marketId, request.toCommand(), LocalDate.now())

        return ApiResponse.success()
    }

    @GetMapping
    override fun getProducts(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @ModelAttribute request: OwnerProductListRequest,
    ): ApiResponse<CursorSliceResponse<OwnerProductListItemResponse>> {
        val slice = productService.getOwnerProducts(apiUser.userId, marketId, request.toSearchCondition(), LocalDate.now())

        return ApiResponse.success(
            CursorSliceResponse(
                content = slice.content.map { OwnerProductListItemResponse(it.product, it.viewCount, it.createdAt) },
                hasNext = slice.hasNext,
                nextCursor = slice.nextCursor,
            ),
        )
    }

    @GetMapping("/{productId}")
    override fun getDetail(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @PathVariable productId: Long,
    ): ApiResponse<OwnerProductDetailResponse> {
        val product = productService.getProduct(apiUser.userId, marketId, productId)

        return ApiResponse.success(OwnerProductDetailResponse(product))
    }

    @PutMapping("/{productId}")
    override fun updateProduct(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @PathVariable productId: Long,
        @RequestBody request: ProductUpdateRequest,
    ): ApiResponse<Unit> {
        productService.updateProduct(apiUser.userId, marketId, productId, request.toCommand(), LocalDate.now())

        return ApiResponse.success()
    }

    @DeleteMapping("/{productId}")
    override fun delete(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @PathVariable productId: Long,
    ): ApiResponse<Unit> {
        productService.delete(apiUser.userId, marketId, productId)

        return ApiResponse.success()
    }

    @DeleteMapping
    override fun deleteAll(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: ProductBulkDeleteRequest,
    ): ApiResponse<Unit> {
        productService.deleteAll(apiUser.userId, marketId, request.productIds)

        return ApiResponse.success()
    }

    @PatchMapping("/discount-period")
    override fun updateDiscountPeriod(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: ProductDiscountPeriodUpdateRequest,
    ): ApiResponse<Unit> {
        productService.updateDiscountPeriod(apiUser.userId, marketId, request.productIds, request.toDiscountPeriod())

        return ApiResponse.success()
    }

    @DeleteMapping("/all")
    override fun reset(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: ProductResetRequest,
    ): ApiResponse<Unit> {
        productService.reset(apiUser.userId, marketId, request.dealType)

        return ApiResponse.success()
    }
}
