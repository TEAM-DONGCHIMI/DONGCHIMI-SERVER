package kr.dongchimi.core.admin

interface AdminSignupCodeVerifier {
    fun isValid(code: String): Boolean
}
