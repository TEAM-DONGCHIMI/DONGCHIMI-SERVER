package kr.dongchimi.api.owner.market

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.market.request.MarketRegisterRequest
import kr.dongchimi.api.owner.market.request.MarketUpdateRequest
import kr.dongchimi.core.market.MarketService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/markets")
class OwnerMarketController(
    private val marketService: MarketService,
) : OwnerMarketApi {
    @PostMapping
    override fun register(
        apiUser: OwnerApiUser,
        @RequestBody request: MarketRegisterRequest,
    ): ApiResponse<Unit> {
        marketService.register(apiUser.userId, request.toCommand())

        return ApiResponse.success()
    }

    @PutMapping("/{marketId}")
    override fun update(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: MarketUpdateRequest,
    ): ApiResponse<Unit> {
        marketService.update(apiUser.userId, marketId, request.toCommand())

        return ApiResponse.success()
    }
}
