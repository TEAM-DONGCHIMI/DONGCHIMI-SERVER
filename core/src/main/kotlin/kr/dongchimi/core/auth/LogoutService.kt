package kr.dongchimi.core.auth

import org.springframework.stereotype.Service

@Service
class LogoutService(
    private val tokenProvider: TokenProvider,
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun logout(refreshTokenValue: String?) {
        val tokenId =
            refreshTokenValue
                ?.let { runCatching { tokenProvider.parseRefreshToken(it) }.getOrNull() }
                ?.tokenId
                ?: return

        refreshTokenRepository.deleteByTokenId(tokenId)
    }
}
