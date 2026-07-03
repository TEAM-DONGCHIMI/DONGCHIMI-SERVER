package kr.dongchimi.api.user

import kr.dongchimi.api.core.ApiUser

data class UserApiUser(
    override val userId: Long,
    override val roles: Set<String>,
) : ApiUser
