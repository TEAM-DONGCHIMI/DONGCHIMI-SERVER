package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.AuthTokens

data class OwnerAuthResult(
    val tokens: AuthTokens,
    val owner: Owner,
    val isAutoLogin: Boolean,
)
