package kr.dongchimi.db.auth

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import kr.dongchimi.core.auth.RefreshToken
import kr.dongchimi.db.common.BaseCreatedTimeEntity
import java.time.LocalDateTime

@Entity
@Table(name = "refresh_tokens")
class RefreshTokenJpaEntity(
    @Id
    val tokenId: String,
    @Column(nullable = false)
    val userId: Long,
    @Column(nullable = false)
    val expiresAt: LocalDateTime,
) : BaseCreatedTimeEntity() {
    constructor(refreshToken: RefreshToken) : this(
        tokenId = refreshToken.tokenId,
        userId = refreshToken.userId,
        expiresAt = refreshToken.expiresAt,
    )

    fun toDomain(): RefreshToken =
        RefreshToken(
            tokenId = tokenId,
            userId = userId,
            expiresAt = expiresAt,
        )
}
