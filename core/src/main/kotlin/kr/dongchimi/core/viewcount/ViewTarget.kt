package kr.dongchimi.core.viewcount

/** 조회수 집계 대상. [key]는 Redis 키 세그먼트로 쓰인다. */
enum class ViewTarget(
    val key: String,
) {
    PRODUCT("product"),
    MARKET("market"),
}
