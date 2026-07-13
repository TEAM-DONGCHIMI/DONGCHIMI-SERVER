package kr.dongchimi.core.owner

import kr.dongchimi.core.market.Market

data class OwnerSignupCompletion(
    val owner: Owner,
    val market: Market,
)
