package kr.dongchimi.core.owner

interface PendingOwnerSignupStore {
    fun save(
        signupToken: String,
        pending: PendingOwnerSignup,
    )

    /** 조회와 삭제를 원자적으로 수행한다. 동시 완료 요청 중 하나만 값을 받고, 이미 소비된 토큰은 재사용할 수 없다. */
    fun consume(signupToken: String): PendingOwnerSignup?
}
