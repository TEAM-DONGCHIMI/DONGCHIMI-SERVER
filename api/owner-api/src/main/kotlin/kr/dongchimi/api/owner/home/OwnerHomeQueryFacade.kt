package kr.dongchimi.api.owner.home

import kr.dongchimi.api.owner.home.response.HomeProductResponse
import kr.dongchimi.api.owner.home.response.OwnerHomeResponse
import kr.dongchimi.core.market.MarketService
import kr.dongchimi.core.product.DealType
import kr.dongchimi.core.product.ProductService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class OwnerHomeQueryFacade(
    private val marketService: MarketService,
    private val productService: ProductService,
) {
    @Transactional(readOnly = true)
    fun getHome(ownerId: Long): OwnerHomeResponse {
        val market =
            marketService.findByOwnerId(ownerId)
                ?: return OwnerHomeResponse(
                    todayRegisteredCount = 0,
                    dailyCount = 0,
                    dailyProducts = emptyList(),
                    periodicCount = 0,
                    periodicProducts = emptyList(),
                )

        val today = LocalDate.now()

        val dailyProducts = productService.getActiveProducts(market.id, DealType.DAILY, today, HOME_PREVIEW_SIZE)
        val periodicProducts = productService.getActiveProducts(market.id, DealType.PERIODIC, today, HOME_PREVIEW_SIZE)

        return OwnerHomeResponse(
            todayRegisteredCount = productService.countRegisteredOn(market.id, today),
            dailyCount = productService.countActiveProducts(market.id, DealType.DAILY, today),
            dailyProducts = dailyProducts.map { HomeProductResponse(it) },
            periodicCount = productService.countActiveProducts(market.id, DealType.PERIODIC, today),
            periodicProducts = periodicProducts.map { HomeProductResponse(it) },
        )
    }

    companion object {
        private const val HOME_PREVIEW_SIZE = 4
    }
}
