package kr.dongchimi.core.auth

interface TokenProvider {
    fun issueAccessToken(
        userId: Long,
        roles: Set<String>,
    ): String
}
