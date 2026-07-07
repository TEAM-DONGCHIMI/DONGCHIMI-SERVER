package kr.dongchimi.core.owner

import kr.dongchimi.core.auth.AuthTokens
import kr.dongchimi.core.market.Market

data class OwnerLoginResult(
    val tokens: AuthTokens,
    val owner: Owner,
    val market: Market?,
    val isAutoLogin: Boolean,
)
