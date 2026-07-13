package kr.dongchimi.core.owner

interface PendingOwnerSignupStore {
    fun save(
        signupToken: String,
        pending: PendingOwnerSignup,
    )

    fun find(signupToken: String): PendingOwnerSignup?

    fun delete(signupToken: String)
}
