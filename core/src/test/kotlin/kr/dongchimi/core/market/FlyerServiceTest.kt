package kr.dongchimi.core.market

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException
import java.util.Base64

class FlyerServiceTest :
    FunSpec({
        fun newService(
            marketRepository: MarketRepository = FakeMarketRepository(),
            flyerRepository: FlyerRepository = FakeFlyerRepository(),
            qrCodeGenerator: QrCodeGenerator = FakeQrCodeGenerator(),
        ): FlyerService =
            FlyerService(
                marketReader = MarketReader(marketRepository),
                marketValidator = MarketValidator(marketRepository),
                flyerReader = FlyerReader(flyerRepository),
                flyerQrManager = FlyerQrManager(qrCodeGenerator),
                flyerAppender = FlyerAppender(flyerRepository, SlugGenerator()),
            )

        test("마트가 없으면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    newService().issueQrCode(ownerId = 1L, marketId = 999L)
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("다른 점주의 마트면 예외가 발생한다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }

            val exception =
                shouldThrow<CoreException> {
                    newService(marketRepository = marketRepository).issueQrCode(ownerId = 2L, marketId = 1L)
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("발행 시 마트가 없으면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    newService().publish(ownerId = 1L, marketId = 999L)
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("발행 시 다른 점주의 마트면 예외가 발생한다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }

            val exception =
                shouldThrow<CoreException> {
                    newService(marketRepository = marketRepository).publish(ownerId = 2L, marketId = 1L)
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_ACCESS_DENIED
        }

        test("전단이 없으면 새로 발행해 slug를 반환한다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }
            val flyerRepository = FakeFlyerRepository()

            val result = newService(marketRepository, flyerRepository).publish(ownerId = 1L, marketId = 1L)

            result shouldBe flyerRepository.findById(1L)?.slug
            flyerRepository.findById(1L)?.qrCode shouldBe null
        }

        test("이미 발행된 경우 기존 slug를 그대로 반환하고 재생성하지 않는다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }
            val flyerRepository =
                FakeFlyerRepository().apply {
                    save(Flyer(id = 1L, slug = "existing-slug", qrCode = "data:image/png;base64,already-issued"))
                }

            val result = newService(marketRepository, flyerRepository).publish(ownerId = 1L, marketId = 1L)

            result shouldBe "existing-slug"
            flyerRepository.findById(1L)?.qrCode shouldBe "data:image/png;base64,already-issued"
        }

        test("전단(slug)이 발행되지 않았으면 예외가 발생한다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }

            val exception =
                shouldThrow<CoreException> {
                    newService(marketRepository = marketRepository).issueQrCode(ownerId = 1L, marketId = 1L)
                }

            exception.errorCode shouldBe FlyerErrorCode.FLYER_NOT_FOUND
        }

        test("QR코드가 없으면 새로 생성해 저장하고 반환한다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }
            val flyerRepository = FakeFlyerRepository().apply { save(Flyer(id = 1L, slug = "gangnam-mart", qrCode = null)) }
            val qrCodeGenerator = FakeQrCodeGenerator()

            val result =
                newService(marketRepository, flyerRepository, qrCodeGenerator)
                    .issueQrCode(ownerId = 1L, marketId = 1L)

            result.qrCode shouldBe "data:image/png;base64,${Base64.getEncoder().encodeToString("gangnam-mart".toByteArray())}"
            flyerRepository.findById(1L)?.qrCode shouldBe result.qrCode
            qrCodeGenerator.callCount shouldBe 1
        }

        test("이미 발행된 경우 저장된 QR코드를 그대로 반환하고 재생성하지 않는다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }
            val flyerRepository =
                FakeFlyerRepository().apply {
                    save(Flyer(id = 1L, slug = "gangnam-mart", qrCode = "data:image/png;base64,already-issued"))
                }
            val qrCodeGenerator = FakeQrCodeGenerator()

            val result =
                newService(marketRepository, flyerRepository, qrCodeGenerator)
                    .issueQrCode(ownerId = 1L, marketId = 1L)

            result.qrCode shouldBe "data:image/png;base64,already-issued"
            qrCodeGenerator.callCount shouldBe 0
        }

        test("공유 정보 조회 시 마트가 없으면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    newService().getShareInfo(marketId = 999L)
                }

            exception.errorCode shouldBe MarketErrorCode.MARKET_NOT_FOUND
        }

        test("공유 정보 조회 시 전단(slug)이 발행되지 않았으면 예외가 발생한다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }

            val exception =
                shouldThrow<CoreException> {
                    newService(marketRepository = marketRepository).getShareInfo(marketId = 1L)
                }

            exception.errorCode shouldBe FlyerErrorCode.FLYER_NOT_FOUND
        }

        test("공유 정보 조회 시 QR코드가 없으면 새로 생성해 저장하고 반환한다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }
            val flyerRepository = FakeFlyerRepository().apply { save(Flyer(id = 1L, slug = "gangnam-mart", qrCode = null)) }
            val qrCodeGenerator = FakeQrCodeGenerator()

            val result =
                newService(marketRepository, flyerRepository, qrCodeGenerator)
                    .getShareInfo(marketId = 1L)

            result.marketId shouldBe 1L
            result.marketName shouldBe "동치미 마트 강남점"
            result.slug shouldBe "gangnam-mart"
            result.qrCode shouldBe "data:image/png;base64,${Base64.getEncoder().encodeToString("gangnam-mart".toByteArray())}"
            flyerRepository.findById(1L)?.qrCode shouldBe result.qrCode
            qrCodeGenerator.callCount shouldBe 1
        }

        test("공유 정보 조회 시 QR코드가 이미 있으면 그대로 반환하고 재생성하지 않는다") {
            val marketRepository = FakeMarketRepository().apply { save(sampleMarket(id = 1L, ownerId = 1L)) }
            val flyerRepository =
                FakeFlyerRepository().apply {
                    save(Flyer(id = 1L, slug = "gangnam-mart", qrCode = "data:image/png;base64,already-issued"))
                }
            val qrCodeGenerator = FakeQrCodeGenerator()

            val result =
                newService(marketRepository, flyerRepository, qrCodeGenerator)
                    .getShareInfo(marketId = 1L)

            result.qrCode shouldBe "data:image/png;base64,already-issued"
            qrCodeGenerator.callCount shouldBe 0
        }
    }) {
    private class FakeMarketRepository : MarketRepository {
        private val store = mutableMapOf<Long, Market>()

        override fun findById(id: Long): Market? = store[id]

        override fun findNearby(
            condition: NearbyMarketSearchCondition,
            limit: Int,
        ): List<NearbyMarket> = emptyList()

        override fun save(market: Market): Market {
            store[market.id] = market
            return market
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

        override fun findByOwnerId(ownerId: Long): Market? = store.values.firstOrNull { it.ownerId == ownerId }

        override fun existsByIdAndOwnerId(
            marketId: Long,
            ownerId: Long,
        ): Boolean = store[marketId]?.ownerId == ownerId

        override fun existsById(id: Long): Boolean = store.containsKey(id)
    }

    private class FakeFlyerRepository : FlyerRepository {
        private val store = mutableMapOf<Long, Flyer>()

        override fun findById(id: Long): Flyer? = store[id]

        override fun findBySlug(slug: String): Flyer? = store.values.firstOrNull { it.slug == slug }

        override fun save(flyer: Flyer): Flyer {
            store[flyer.id] = flyer
            return flyer
        }
    }

    private class FakeQrCodeGenerator : QrCodeGenerator {
        var callCount = 0
            private set

        override fun generate(slug: String): ByteArray {
            callCount++
            return slug.toByteArray()
        }
    }
}

private fun sampleMarket(
    id: Long,
    ownerId: Long,
): Market =
    Market(
        id = id,
        ownerId = ownerId,
        info = MarketInfo(name = "동치미 마트 강남점", address = "서울특별시 성북구 정릉동 880-20", thumbnailUrl = null),
        location = LocationPoint(longitude = 127.0, latitude = 37.0),
        businessHours = BusinessHours(emptyList()),
        phoneNumber =
            MarketPhoneNumber(
                marketPhone1 = "02-000-0000",
                marketPhone2 = null,
                marketPhonePrimary = 1,
                ownerPhone = "010-0000-0000",
            ),
        brn = null,
    )
