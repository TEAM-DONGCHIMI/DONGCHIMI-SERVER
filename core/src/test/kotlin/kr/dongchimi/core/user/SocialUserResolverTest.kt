package kr.dongchimi.core.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.auth.SocialUserInfo
import kr.dongchimi.core.user.exception.DuplicateSocialAccountException

class SocialUserResolverTest :
    FunSpec({
        test("가입 중 유니크 제약 위반이 나면 재조회로 기존 유저를 반환한다") {
            val account = SocialAccount(SocialProvider.KAKAO, "social-1")
            val existing = User(id = 1L, email = "a@dongchimi.kr", name = "동치미", socialAccount = account, gender = Gender.M, age = null)
            val userRepository = FakeUserRepository(seed = listOf(existing), throwOnSave = true)
            val resolver = SocialUserResolver(UserReader(userRepository), UserAppender(userRepository))
            val info = SocialUserInfo(account, "a@dongchimi.kr", "동치미", Gender.M, null)

            val resolved = resolver.resolve(info)

            resolved shouldBe existing
        }

        test("재조회도 실패하면 원래 예외를 던진다") {
            val account = SocialAccount(SocialProvider.KAKAO, "social-1")
            val userRepository = FakeUserRepository(throwOnSave = true)
            val resolver = SocialUserResolver(UserReader(userRepository), UserAppender(userRepository))
            val info = SocialUserInfo(account, "a@dongchimi.kr", "동치미", Gender.M, null)

            shouldThrow<DuplicateSocialAccountException> {
                resolver.resolve(info)
            }
        }
    }) {
    private class FakeUserRepository(
        seed: List<User> = emptyList(),
        private val throwOnSave: Boolean = false,
    ) : UserRepository {
        private val store = mutableMapOf<Long, User>()
        private var nextId = 1L

        init {
            seed.forEach { store[it.id] = it }
        }

        override fun findById(id: Long): User? = store[id]

        override fun findBySocialAccount(account: SocialAccount): User? = store.values.find { it.socialAccount == account }

        override fun save(user: User): User {
            if (throwOnSave) throw DuplicateSocialAccountException()

            val saved = if (user.id == 0L) user.copy(id = nextId++) else user
            store[saved.id] = saved
            return saved
        }
    }
}
