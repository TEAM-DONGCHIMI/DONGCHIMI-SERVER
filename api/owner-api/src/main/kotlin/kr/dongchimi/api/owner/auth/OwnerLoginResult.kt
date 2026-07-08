package kr.dongchimi.api.owner.auth

import kr.dongchimi.api.owner.auth.response.OwnerLoginResponse
import java.time.LocalDateTime

data class OwnerLoginResult(
    val response: OwnerLoginResponse,
    val refreshToken: String,
    val refreshExpiresAt: LocalDateTime,
    val isAutoLogin: Boolean,
)
