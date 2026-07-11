package kr.dongchimi.core.admin

import kr.dongchimi.core.auth.AuthTokens

data class AdminAuthResult(
    val tokens: AuthTokens,
    val admin: Admin,
    val isAutoLogin: Boolean,
)
