package kr.dongchimi.core.auth

interface RefreshTokenRepository {
    fun save(refreshToken: RefreshToken): RefreshToken

    fun deleteByTokenId(tokenId: String): Long
}
