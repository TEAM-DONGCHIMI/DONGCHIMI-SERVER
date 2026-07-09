package kr.dongchimi.core.market

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.DayOfWeek
import java.time.LocalTime

class MarketFinderTest :
    FunSpec({
        val condition =
            NearbyMarketSearchCondition(
                origin = LocationPoint(longitude = 126.98, latitude = 37.55),
                radiusMeters = 1000.0,
                cursorMarketId = null,
                size = 2,
            )

        test("size보다 많이 조회되면 hasNext가 true이고 size개로 자른다") {
            val repository = StubMarketRepository(listOf(nearbyMarket(1L), nearbyMarket(2L), nearbyMarket(3L)))

            val result = MarketFinder(repository).findNearby(condition)

            repository.requestedLimit shouldBe 3
            result.hasNext shouldBe true
            result.content.map { it.market.id } shouldBe listOf(1L, 2L)
        }

        test("다음 페이지가 있으면 마지막 마트 id를 nextCursor로 준다") {
            val repository = StubMarketRepository(listOf(nearbyMarket(1L), nearbyMarket(2L), nearbyMarket(3L)))

            val result = MarketFinder(repository).findNearby(condition)

            result.nextCursor shouldBe 2L
        }

        test("size 이하로 조회되면 hasNext가 false이고 nextCursor는 null이다") {
            val repository = StubMarketRepository(listOf(nearbyMarket(1L), nearbyMarket(2L)))

            val result = MarketFinder(repository).findNearby(condition)

            result.hasNext shouldBe false
            result.nextCursor shouldBe null
            result.content.map { it.market.id } shouldBe listOf(1L, 2L)
        }

        test("반경 내 마트가 없으면 빈 결과를 반환한다") {
            val result = MarketFinder(StubMarketRepository(emptyList())).findNearby(condition)

            result.hasNext shouldBe false
            result.nextCursor shouldBe null
            result.content shouldBe emptyList()
        }
    })

private fun nearbyMarket(id: Long) =
    NearbyMarket(
        market =
            Market(
                id = id,
                ownerId = 1L,
                info = MarketInfo(name = "마트$id", address = "서울시 마포구 망원동", thumbnailUrl = null),
                location = LocationPoint(longitude = 126.98, latitude = 37.55),
                businessHours =
                    BusinessHours(
                        listOf(
                            BusinessHourSlot(
                                days = listOf(DayOfWeek.MONDAY),
                                isOpen = true,
                                open = LocalTime.of(10, 0),
                                close = LocalTime.of(20, 0),
                            ),
                        ),
                    ),
                phoneNumber = MarketPhoneNumber("02-123-4567", null, 1, "010-1234-5678"),
                brn = null,
            ),
        slug = "slug-$id",
    )

private class StubMarketRepository(
    private val markets: List<NearbyMarket>,
) : MarketRepository {
    var requestedLimit: Int = 0
        private set

    override fun findNearby(
        condition: NearbyMarketSearchCondition,
        limit: Int,
    ): List<NearbyMarket> {
        requestedLimit = limit
        return markets.take(limit)
    }

    override fun findById(id: Long): Market? = throw UnsupportedOperationException()

    override fun findByOwnerId(ownerId: Long): Market? = throw UnsupportedOperationException()

    override fun save(market: Market): Market = throw UnsupportedOperationException()

    override fun existsByOwnerIdAndName(
        ownerId: Long,
        name: String,
    ): Boolean = throw UnsupportedOperationException()

    override fun existsByOwnerIdAndNameAndIdNot(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean = throw UnsupportedOperationException()

    override fun existsByIdAndOwnerId(
        marketId: Long,
        ownerId: Long,
    ): Boolean = throw UnsupportedOperationException()

    override fun existsById(id: Long): Boolean = throw UnsupportedOperationException()
}
