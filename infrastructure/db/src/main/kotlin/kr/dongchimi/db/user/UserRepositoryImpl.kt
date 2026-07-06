package kr.dongchimi.db.user

import kr.dongchimi.core.user.SocialAccount
import kr.dongchimi.core.user.User
import kr.dongchimi.core.user.UserRepository
import kr.dongchimi.core.user.exception.DuplicateSocialAccountException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository,
) : UserRepository {
    override fun findById(id: Long): User? = userJpaRepository.findByIdAndDeletedAtIsNull(id)?.toDomain()

    override fun findBySocialAccount(account: SocialAccount): User? =
        userJpaRepository.findBySocialProviderAndSocialIdAndDeletedAtIsNull(account.provider, account.socialId)?.toDomain()

    override fun save(user: User): User =
        try {
            userJpaRepository.save(UserJpaEntity(user)).toDomain()
        } catch (exception: DataIntegrityViolationException) {
            if (exception.isSocialAccountUniqueViolation()) {
                throw DuplicateSocialAccountException(cause = exception)
            }
            throw exception
        }

    private fun DataIntegrityViolationException.isSocialAccountUniqueViolation(): Boolean =
        mostSpecificCause.message?.contains(SOCIAL_ACCOUNT_UNIQUE_INDEX) == true

    companion object {
        private const val SOCIAL_ACCOUNT_UNIQUE_INDEX = "uq_users_social_account_active"
    }
}
