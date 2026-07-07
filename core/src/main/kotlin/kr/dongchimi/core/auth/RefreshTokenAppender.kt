package kr.dongchimi.core.auth

import org.springframework.stereotype.Component

@Component
class RefreshTokenAppender(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    fun append(
        issued: IssuedRefreshToken,
        userId: Long,
    ): RefreshToken =
        refreshTokenRepository.save(
            RefreshToken(
                issued,
                userId,
            ),
        )
}
