package kr.dongchimi.api.admin

import kr.dongchimi.api.core.ApiUser

data class AdminApiUser(
    override val userId: Long,
    override val roles: Set<String>,
) : ApiUser
