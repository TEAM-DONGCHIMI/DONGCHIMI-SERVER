package kr.dongchimi.common.utils

object RegexPatterns {
    const val EMAIL_PATTERN = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    const val HANGUL_PATTERN = "[가-힣ㄱ-ㅎㅏ-ㅣ]"

    private val emailRegex = Regex(EMAIL_PATTERN)
    private val hangulRegex = Regex(HANGUL_PATTERN)

    fun String.isEmail(): Boolean = emailRegex.matches(this)

    fun String.containsHangul(): Boolean = hangulRegex.containsMatchIn(this)
}
