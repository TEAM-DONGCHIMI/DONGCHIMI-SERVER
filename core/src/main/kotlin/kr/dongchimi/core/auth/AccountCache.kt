package kr.dongchimi.core.auth

/** role별 계정 "존재함" 여부를 캐싱하는 공용 포트. 존재만 저장하고 미존재는 저장하지 않는다. */
interface AccountCache {
    fun isKnownToExist(
        role: Role,
        id: Long,
    ): Boolean

    fun markExists(
        role: Role,
        id: Long,
    )

    fun evict(
        role: Role,
        id: Long,
    )
}
