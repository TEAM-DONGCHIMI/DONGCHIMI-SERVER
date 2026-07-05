package kr.dongchimi.api.owner

import kr.dongchimi.api.core.ApiUser

data class OwnerApiUser(
    override val userId: Long,
    override val roles: Set<String>,
) : ApiUser
