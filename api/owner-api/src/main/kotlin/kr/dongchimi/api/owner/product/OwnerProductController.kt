package kr.dongchimi.api.owner.product

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.PageOffsetRequest
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.PreparedProductDraftSearchRequest
import kr.dongchimi.api.owner.product.request.ProductBulkDeleteRequest
import kr.dongchimi.api.owner.product.request.ProductDiscountPeriodUpdateRequest
import kr.dongchimi.api.owner.product.request.ProductResetRequest
import kr.dongchimi.api.owner.product.response.OwnerPreparedProductDraftListResponse
import kr.dongchimi.api.owner.product.response.OwnerProductDetailResponse
import kr.dongchimi.core.product.ProductService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/markets/{marketId}/products")
class OwnerProductController(
    private val productService: ProductService,
    private val ownerPreparedProductDraftQueryFacade: OwnerPreparedProductDraftQueryFacade,
) : OwnerProductApi {
    @GetMapping("/draft")
    override fun getDrafts(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @ModelAttribute request: PreparedProductDraftSearchRequest,
        @ModelAttribute pageOffsetRequest: PageOffsetRequest,
    ): ApiResponse<OwnerPreparedProductDraftListResponse> {
        val response =
            ownerPreparedProductDraftQueryFacade.getDrafts(
                apiUser.userId,
                marketId,
                request.toSearchCondition(),
                pageOffsetRequest.toPageOffset(),
            )

        return ApiResponse.success(response)
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
