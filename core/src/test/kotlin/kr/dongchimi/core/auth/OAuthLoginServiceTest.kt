package kr.dongchimi.core.auth

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.Gender
import kr.dongchimi.core.user.SocialAccount
import kr.dongchimi.core.user.SocialProvider
import kr.dongchimi.core.user.User
import kr.dongchimi.core.user.UserAppender
import kr.dongchimi.core.user.UserReader
import kr.dongchimi.core.user.UserRepository

class OAuthLoginServiceTest :
    FunSpec({
        fun service(
            userRepository: UserRepository,
            vararg clients: OAuthUserClient,
        ) = OAuthLoginService(
            SocialUserInfoReader(clients.toList()),
            UserReader(userRepository),
            UserAppender(userRepository),
            FakeTokenProvider(),
        )

        test("기존 유저는 재가입 없이 토큰을 발급받는다") {
            val userRepository = FakeUserRepository()
            val account = SocialAccount(SocialProvider.KAKAO, "social-1")
            val existing =
                userRepository.save(
                    User(
                        email = "a@dongchimi.kr",
                        name = "동치미",
                        socialAccount = account,
                        gender = Gender.M,
                        age = null,
                    ),
                )
            val client = FakeOAuthUserClient(SocialProvider.KAKAO, SocialUserInfo(account, "a@dongchimi.kr", "동치미", Gender.M, null))

            val token = service(userRepository, client).login(OAuthLoginCommand(SocialProvider.KAKAO, "kakao-token"))

            token shouldBe "token-${existing.id}"
            userRepository.size shouldBe 1
        }

        test("신규 유저는 자동 가입 후 토큰을 발급받는다") {
            val userRepository = FakeUserRepository()
            val account = SocialAccount(SocialProvider.KAKAO, "social-1")
            val client = FakeOAuthUserClient(SocialProvider.KAKAO, SocialUserInfo(account, "a@dongchimi.kr", "동치미", Gender.M, null))

            val token = service(userRepository, client).login(OAuthLoginCommand(SocialProvider.KAKAO, "kakao-token"))

            val saved = userRepository.findBySocialAccount(account)
            token shouldBe "token-${saved!!.id}"
            userRepository.size shouldBe 1
        }

        test("지원하지 않는 provider면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    service(FakeUserRepository()).login(OAuthLoginCommand(SocialProvider.KAKAO, "kakao-token"))
                }

            exception.errorCode shouldBe AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER
        }

        test("이메일이 없으면 예외가 발생한다") {
            val account = SocialAccount(SocialProvider.KAKAO, "social-1")
            val client = FakeOAuthUserClient(SocialProvider.KAKAO, SocialUserInfo(account, null, "동치미", Gender.M, null))

            val exception =
                shouldThrow<CoreException> {
                    service(FakeUserRepository(), client).login(OAuthLoginCommand(SocialProvider.KAKAO, "kakao-token"))
                }

            exception.errorCode shouldBe AuthErrorCode.OAUTH_REQUIRED_INFO_MISSING
        }

        test("성별이 없으면 예외가 발생한다") {
            val account = SocialAccount(SocialProvider.KAKAO, "social-1")
            val client = FakeOAuthUserClient(SocialProvider.KAKAO, SocialUserInfo(account, "a@dongchimi.kr", "동치미", null, null))

            val exception =
                shouldThrow<CoreException> {
                    service(FakeUserRepository(), client).login(OAuthLoginCommand(SocialProvider.KAKAO, "kakao-token"))
                }

            exception.errorCode shouldBe AuthErrorCode.OAUTH_REQUIRED_INFO_MISSING
        }
    }) {
    private class FakeUserRepository : UserRepository {
        private val store = mutableMapOf<Long, User>()
        private var nextId = 1L

        val size: Int get() = store.size

        override fun findById(id: Long): User? = store[id]

        override fun findBySocialAccount(account: SocialAccount): User? = store.values.find { it.socialAccount == account }

        override fun save(user: User): User {
            val saved = if (user.id == 0L) user.copy(id = nextId++) else user
            store[saved.id] = saved
            return saved
        }
    }

    private class FakeOAuthUserClient(
        override val provider: SocialProvider,
        private val info: SocialUserInfo,
    ) : OAuthUserClient {
        override fun fetchUserInfo(accessToken: String): SocialUserInfo = info
    }

    private class FakeTokenProvider : TokenProvider {
        override fun issueAccessToken(
            userId: Long,
            roles: Set<String>,
        ): String = "token-$userId"
    }
}
