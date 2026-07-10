package kr.dongchimi.api.user.product

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.dto.CursorSliceResponse
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.product.request.PeriodicProductListRequest
import kr.dongchimi.api.user.product.response.DailyDealListResponse
import kr.dongchimi.api.user.product.response.PeriodicProductResponse
import kr.dongchimi.api.user.product.response.ProductDetailResponse
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.ProductService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/v1/users/markets/{marketId}/products")
class UserProductController(
    private val productService: ProductService,
    private val productDetailQueryFacade: ProductDetailQueryFacade,
) : UserProductApi {
    @GetMapping("/daily")
    override fun getDailyDeals(
        apiUser: UserApiUser,
        @PathVariable marketId: Long,
    ): ApiResponse<DailyDealListResponse> {
        val products = productService.getAllActiveProducts(marketId, DealType.DAILY, LocalDate.now())

        return ApiResponse.success(DailyDealListResponse(products))
    }

    @GetMapping("/{productId}")
    override fun getDetail(
        apiUser: UserApiUser,
        @PathVariable marketId: Long,
        @PathVariable productId: Long,
    ): ApiResponse<ProductDetailResponse> = ApiResponse.success(productDetailQueryFacade.getDetail(marketId, productId))

    @GetMapping("/periodic")
    override fun getPeriodicDeals(
        apiUser: UserApiUser,
        @PathVariable marketId: Long,
        request: PeriodicProductListRequest,
    ): ApiResponse<CursorSliceResponse<PeriodicProductResponse>> {
        val slice = productService.getActiveProductsByCategory(marketId, DealType.PERIODIC, request.toSearchCondition(), LocalDate.now())

        return ApiResponse.success(
            CursorSliceResponse(
                content = slice.content.map { PeriodicProductResponse(it) },
                hasNext = slice.hasNext,
                nextCursor = slice.nextCursor,
            ),
        )
    }
}
