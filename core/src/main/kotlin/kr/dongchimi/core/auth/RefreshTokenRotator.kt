package kr.dongchimi.core.auth

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class RefreshTokenRotator(
    private val refreshTokenRepository: RefreshTokenRepository,
) {
    @Transactional
    fun rotate(
        oldTokenId: String,
        issued: IssuedRefreshToken,
        userId: Long,
    ): RefreshToken {
        val deletedCount = refreshTokenRepository.deleteByTokenId(oldTokenId)
        if (deletedCount == 0L) throw CoreException(AuthErrorCode.INVALID_REFRESH_TOKEN)

        return refreshTokenRepository.save(RefreshToken(issued, userId))
    }
}
