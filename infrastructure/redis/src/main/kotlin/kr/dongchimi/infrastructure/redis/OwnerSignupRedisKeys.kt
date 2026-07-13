package kr.dongchimi.infrastructure.redis

internal object OwnerSignupRedisKeys {
    fun pending(signupToken: String) = "owner:signup:pending:$signupToken"
}
