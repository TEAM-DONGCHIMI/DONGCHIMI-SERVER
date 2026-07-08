package kr.dongchimi.core.owner

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.dongchimi.core.auth.PasswordEncoder
import kr.dongchimi.core.common.exception.CoreException

class OwnerSignupServiceTest :
    FunSpec({
        fun service(repository: OwnerRepository): OwnerSignupService {
            val encoder = PrefixPasswordEncoder()
            return OwnerSignupService(OwnerReader(repository), OwnerAppender(repository, encoder))
        }

        test("정상 가입 시 비밀번호를 해싱해 저장하고 생성된 점주를 반환한다") {
            val repository = FakeOwnerRepository()

            val owner = service(repository).signup(OwnerSignupCommand("owner@dongchimi.kr", "password123!"))

            owner.id shouldNotBe 0L
            owner.email shouldBe "owner@dongchimi.kr"
            owner.password shouldBe "encoded:password123!"
            owner.password shouldNotBe "password123!"
        }

        test("이미 가입된 이메일이면 DUPLICATE_EMAIL 예외를 던진다") {
            val existing = Owner(id = 1L, email = "owner@dongchimi.kr", password = "encoded:pw")
            val repository = FakeOwnerRepository(seed = listOf(existing))

            val exception =
                shouldThrow<CoreException> {
                    service(repository).signup(OwnerSignupCommand("owner@dongchimi.kr", "password123!"))
                }

            exception.errorCode shouldBe OwnerErrorCode.DUPLICATE_EMAIL
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
        private val store = mutableMapOf<Long, Owner>()
        private var nextId = 1L

        init {
            seed.forEach { store[it.id] = it }
        }

        override fun findById(id: Long): Owner? = store[id]

        override fun findByEmail(email: String): Owner? = store.values.find { it.email == email }

        override fun existsByEmail(email: String): Boolean = store.values.any { it.email == email }

        override fun save(owner: Owner): Owner {
            val saved = if (owner.id == 0L) owner.copy(id = nextId++) else owner
            store[saved.id] = saved
            return saved
        }
    }
}
