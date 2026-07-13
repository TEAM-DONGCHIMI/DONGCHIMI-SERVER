package kr.dongchimi.infrastructure.redis

import kr.dongchimi.core.viewcount.ViewTarget

internal object ViewCountRedisKeys {
    fun dedup(
        target: ViewTarget,
        targetId: Long,
        userId: Long,
    ) = "view:dedup:${target.key}:$targetId:$userId"

    fun pending(target: ViewTarget) = "view:pending:${target.key}"

    fun flush(
        target: ViewTarget,
        token: String,
    ) = "view:pending:${target.key}:flush:$token"
}
