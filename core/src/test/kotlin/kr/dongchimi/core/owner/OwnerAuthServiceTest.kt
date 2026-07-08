package kr.dongchimi.core.owner

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.dongchimi.core.auth.AuthTokenIssuer
import kr.dongchimi.core.auth.IssuedRefreshToken
import kr.dongchimi.core.auth.PasswordEncoder
import kr.dongchimi.core.auth.RefreshToken
import kr.dongchimi.core.auth.RefreshTokenAppender
import kr.dongchimi.core.auth.RefreshTokenPayload
import kr.dongchimi.core.auth.RefreshTokenRepository
import kr.dongchimi.core.auth.Role
import kr.dongchimi.core.auth.TokenProvider
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.market.LocationPoint
import kr.dongchimi.core.market.Market
import kr.dongchimi.core.market.MarketPhoneNumber
import kr.dongchimi.core.market.MarketReader
import kr.dongchimi.core.market.MarketRepository
import java.time.LocalDateTime

private const val EMAIL = "owner@dongchimi.kr"
private const val RAW_PASSWORD = "password123!"
private const val ENCODED_PASSWORD = "encoded:password123!"

class OwnerAuthServiceTest :
    FunSpec({
        fun ownerWithPassword(encoded: String = ENCODED_PASSWORD) = Owner(id = 1L, email = EMAIL, password = encoded)

        fun sampleMarket(ownerId: Long) =
            Market(
                id = 10L,
                ownerId = ownerId,
                name = "신선마트",
                address = "서울시 어딘가",
                thumbnailUrl = "https://cdn.example.com/market/10.png",
                location = LocationPoint(127.0, 37.0),
                businessHours = null,
                phoneNumber = MarketPhoneNumber("02-000-0000", null, 1, "010-0000-0000", null, 1),
                brn = null,
            )

        fun service(
            owners: List<Owner> = emptyList(),
            markets: List<Market> = emptyList(),
        ): OwnerAuthService {
            val ownerRepository = FakeOwnerRepository(owners)
            return OwnerAuthService(
                OwnerReader(ownerRepository),
                OwnerAppender(ownerRepository, PrefixPasswordEncoder()),
                MarketReader(FakeMarketRepository(markets)),
                PrefixPasswordEncoder(),
                AuthTokenIssuer(FakeTokenProvider(), RefreshTokenAppender(FakeRefreshTokenRepository())),
            )
        }

        test("정상 가입 시 비밀번호를 해싱해 저장하고 생성된 점주를 반환한다") {
            val owner = service().signup(OwnerSignupCommand(EMAIL, RAW_PASSWORD))

            owner.id shouldNotBe 0L
            owner.email shouldBe EMAIL
            owner.password shouldBe ENCODED_PASSWORD
            owner.password shouldNotBe RAW_PASSWORD
        }

        test("이미 가입된 이메일이면 DUPLICATE_EMAIL 예외를 던진다") {
            val exception =
                shouldThrow<CoreException> {
                    service(owners = listOf(ownerWithPassword())).signup(OwnerSignupCommand(EMAIL, RAW_PASSWORD))
                }

            exception.errorCode shouldBe OwnerErrorCode.DUPLICATE_EMAIL
        }

        test("가입되지 않은 이메일이면 LOGIN_FAILED를 던진다") {
            val exception =
                shouldThrow<CoreException> {
                    service().login(OwnerLoginCommand(EMAIL, RAW_PASSWORD, isAutoLogin = true))
                }

            exception.errorCode shouldBe OwnerErrorCode.LOGIN_FAILED
        }

        test("비밀번호가 일치하지 않으면 LOGIN_FAILED를 던진다") {
            val exception =
                shouldThrow<CoreException> {
                    service(owners = listOf(ownerWithPassword()))
                        .login(OwnerLoginCommand(EMAIL, "wrong-password", isAutoLogin = true))
                }

            exception.errorCode shouldBe OwnerErrorCode.LOGIN_FAILED
        }

        test("정상 로그인 시 토큰을 발급하고 isAutoLogin과 마트를 함께 반환한다") {
            val market = sampleMarket(ownerId = 1L)

            val result =
                service(owners = listOf(ownerWithPassword()), markets = listOf(market))
                    .login(OwnerLoginCommand(EMAIL, RAW_PASSWORD, isAutoLogin = false))

            result.tokens.accessToken shouldBe "token-1"
            result.tokens.refreshToken shouldBe "refresh-1"
            result.owner.id shouldBe 1L
            result.market shouldBe market
            result.isAutoLogin shouldBe false
        }

        test("마트가 없는 점주도 로그인에 성공하며 market은 null이다") {
            val result =
                service(owners = listOf(ownerWithPassword()))
                    .login(OwnerLoginCommand(EMAIL, RAW_PASSWORD, isAutoLogin = true))

            result.market shouldBe null
            result.isAutoLogin shouldBe true
        }
    }) {
    private class PrefixPasswordEncoder : PasswordEncoder {
        override fun encode(rawPassword: String): String = "encoded:$rawPassword"

        override fun matches(
            rawPassword: String,
            encodedPassword: String,
        ): Boolean = encodedPassword == "encoded:$rawPassword"
    }

    private class FakeOwnerRepository(
        seed: List<Owner> = emptyList(),
    ) : OwnerRepository {
        private val store = seed.associateBy { it.id }.toMutableMap()
        private var nextId = (seed.maxOfOrNull { it.id } ?: 0L) + 1L

        override fun findById(id: Long): Owner? = store[id]

        override fun findByEmail(email: String): Owner? = store.values.find { it.email == email }

        override fun existsByEmail(email: String): Boolean = store.values.any { it.email == email }

        override fun save(owner: Owner): Owner {
            val saved = if (owner.id == 0L) owner.copy(id = nextId++) else owner
            store[saved.id] = saved
            return saved
        }
    }

    private class FakeMarketRepository(
        seed: List<Market> = emptyList(),
    ) : MarketRepository {
        private val store = seed.associateBy { it.id }.toMutableMap()

        override fun findById(id: Long): Market? = store[id]

        override fun findByOwnerId(ownerId: Long): Market? = store.values.find { it.ownerId == ownerId }

        override fun save(market: Market): Market {
            store[market.id] = market
            return market
        }
    }

    private class FakeTokenProvider : TokenProvider {
        override fun issueAccessToken(
            userId: Long,
            roles: Set<String>,
        ): String = "token-$userId"

        override fun issueRefreshToken(
            userId: Long,
            roles: Set<String>,
        ): IssuedRefreshToken = IssuedRefreshToken("refresh-$userId", "token-id-$userId", LocalDateTime.MAX)

        override fun parseRefreshToken(tokenValue: String): RefreshTokenPayload =
            RefreshTokenPayload("token-id", 1L, setOf(Role.OWNER.name))
    }

    private class FakeRefreshTokenRepository : RefreshTokenRepository {
        private val store = mutableMapOf<String, RefreshToken>()

        override fun save(refreshToken: RefreshToken): RefreshToken {
            store[refreshToken.tokenId] = refreshToken
            return refreshToken
        }

        override fun deleteByTokenId(tokenId: String): Long = if (store.remove(tokenId) != null) 1L else 0L
    }
}
