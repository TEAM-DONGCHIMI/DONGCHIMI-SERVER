package kr.dongchimi.db.auth

import kr.dongchimi.core.auth.RefreshToken
import kr.dongchimi.core.auth.RefreshTokenRepository
import org.springframework.stereotype.Repository

@Repository
class RefreshTokenRepositoryImpl(
    private val refreshTokenJpaRepository: RefreshTokenJpaRepository,
) : RefreshTokenRepository {
    override fun save(refreshToken: RefreshToken): RefreshToken =
        refreshTokenJpaRepository.save(RefreshTokenJpaEntity(refreshToken)).toDomain()

    override fun deleteByTokenId(tokenId: String): Long = refreshTokenJpaRepository.deleteByTokenId(tokenId)
}
