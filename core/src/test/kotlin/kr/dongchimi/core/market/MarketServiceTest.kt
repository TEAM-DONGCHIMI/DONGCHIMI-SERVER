package kr.dongchimi.core.market

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException

class MarketServiceTest :
    FunSpec({
        fun newService(repository: MarketRepository = FakeMarketRepository()): Pair<MarketService, MarketRepository> {
            val service =
                MarketService(
                    marketReader = MarketReader(repository),
                    marketAppender = MarketAppender(repository),
                    marketUpdater = MarketUpdater(repository),
                    marketValidator = MarketValidator(repository),
                )
            return service to repository
        }

        test("등록 시 같은 점주에 동일한 이름의 마트가 없으면 저장한다") {
            val (service, _) = newService()

            val market = service.register(ownerId = 1L, command = sampleRegisterCommand())

            market.id shouldBe 1L
            market.ownerId shouldBe 1L
            market.info.name shouldBe "동치미 마트 강남점"
        }

        test("등록 시 같은 점주에 동일한 이름의 마트가 이미 있으면 예외가 발생한다") {
            val repository = FakeMarketRepository()
            val (service, _) = newService(repository)
            service.register(ownerId = 1L, command = sampleRegisterCommand())

            val exception =
                shouldThrow<CoreException> {
                    service.register(ownerId = 1L, command = sampleRegisterCommand())
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ALREADY_EXISTS
        }

        test("수정 대상 마트가 없으면 예외가 발생한다") {
            val (service, _) = newService()

            val exception =
                shouldThrow<CoreException> {
                    service.update(ownerId = 1L, marketId = 999L, command = sampleUpdateCommand())
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("수정 대상 마트가 다른 점주 소유면 예외가 발생한다") {
            val (service, _) = newService()
            val market = service.register(ownerId = 1L, command = sampleRegisterCommand())

            val exception =
                shouldThrow<CoreException> {
                    service.update(ownerId = 2L, marketId = market.id, command = sampleUpdateCommand())
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("수정 시 같은 점주의 다른 마트와 이름이 겹치면 예외가 발생한다") {
            val (service, _) = newService()
            service.register(ownerId = 1L, command = sampleRegisterCommand(name = "동치미 마트 강남점"))
            val other = service.register(ownerId = 1L, command = sampleRegisterCommand(name = "동치미 마트 서초점"))

            val exception =
                shouldThrow<CoreException> {
                    service.update(ownerId = 1L, marketId = other.id, command = sampleUpdateCommand(name = "동치미 마트 강남점"))
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ALREADY_EXISTS
        }

        test("정상 수정이면 변경된 내용이 반영된 마트를 반환한다") {
            val (service, _) = newService()
            val market = service.register(ownerId = 1L, command = sampleRegisterCommand())

            val updated =
                service.update(
                    ownerId = 1L,
                    marketId = market.id,
                    command = sampleUpdateCommand(name = "동치미 마트 강남 2호점"),
                )

            updated.id shouldBe market.id
            updated.info.name shouldBe "동치미 마트 강남 2호점"
        }
    })

private fun sampleRegisterCommand(name: String = "동치미 마트 강남점"): MarketRegisterCommand =
    MarketRegisterCommand(
        info = MarketInfo(name = name, address = "서울특별시 성북구 정릉동 880-20", thumbnailUrl = null),
        location = LocationPoint(longitude = 127.0, latitude = 37.0),
        businessHours = BusinessHours(),
        phoneNumber =
            MarketPhoneNumber(
                marketPhone1 = "02-000-0000",
                marketPhone2 = null,
                marketPhonePrimary = 1,
                ownerPhone = "010-0000-0000",
            ),
        brn = null,
    )

private fun sampleUpdateCommand(name: String = "동치미 마트 강남점"): MarketUpdateCommand =
    MarketUpdateCommand(
        info = MarketInfo(name = name, address = "서울특별시 성북구 정릉동 880-20", thumbnailUrl = null),
        location = LocationPoint(longitude = 127.0, latitude = 37.0),
        businessHours = BusinessHours(),
        phoneNumber =
            MarketPhoneNumber(
                marketPhone1 = "02-000-0000",
                marketPhone2 = null,
                marketPhonePrimary = 1,
                ownerPhone = "010-0000-0000",
            ),
        brn = null,
    )

private class FakeMarketRepository : MarketRepository {
    private val store = mutableMapOf<Long, Market>()
    private var nextId = 1L

    override fun findById(id: Long): Market? = store[id]

    override fun findByOwnerId(ownerId: Long): Market? = store.values.find { it.ownerId == ownerId }

    override fun save(market: Market): Market {
        val id = if (market.id == 0L) nextId++ else market.id
        val saved = market.copy(id = id)
        store[id] = saved
        return saved
    }

    override fun existsByOwnerIdAndName(
        ownerId: Long,
        name: String,
    ): Boolean = store.values.any { it.ownerId == ownerId && it.info.name == name }

    override fun existsByOwnerIdAndNameAndIdNot(
        ownerId: Long,
        name: String,
        id: Long,
    ): Boolean = store.values.any { it.ownerId == ownerId && it.info.name == name && it.id != id }

    override fun existsByIdAndOwnerId(
        marketId: Long,
        ownerId: Long,
    ): Boolean = store[marketId]?.ownerId == ownerId

    override fun existsById(id: Long): Boolean = store.containsKey(id)
}
