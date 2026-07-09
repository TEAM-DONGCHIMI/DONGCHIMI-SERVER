package kr.dongchimi.api.user.market

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.user.UserApiUser
import kr.dongchimi.api.user.market.response.MarketDetailResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("/v1/users/markets")
class UserMarketController(
    private val marketDetailQueryFacade: MarketDetailQueryFacade,
) : UserMarketApi {
    @GetMapping("/{slug}")
    override fun getDetail(
        apiUser: UserApiUser,
        @PathVariable slug: String,
    ): ApiResponse<MarketDetailResponse> = ApiResponse.success(marketDetailQueryFacade.getDetail(slug, LocalDateTime.now()))
}
