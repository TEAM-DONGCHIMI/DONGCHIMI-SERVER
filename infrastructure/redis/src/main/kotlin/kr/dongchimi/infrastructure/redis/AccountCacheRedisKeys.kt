package kr.dongchimi.infrastructure.redis

import kr.dongchimi.core.auth.Role

internal object AccountCacheRedisKeys {
    // user:exists:1 / owner:exists:1 / admin:exists:1
    fun exists(
        role: Role,
        id: Long,
    ) = "${role.name.lowercase()}:exists:$id"
}
