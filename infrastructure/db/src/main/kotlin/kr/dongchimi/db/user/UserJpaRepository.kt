package kr.dongchimi.db.user

import kr.dongchimi.core.user.SocialProvider
import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): UserJpaEntity?

    fun existsByIdAndDeletedAtIsNull(id: Long): Boolean

    fun findBySocialProviderAndSocialIdAndDeletedAtIsNull(
        socialProvider: SocialProvider,
        socialId: String,
    ): UserJpaEntity?
}
