package kr.dongchimi.common.utils

object HangulUtils {
    private const val HANGUL_BASE = 0xAC00
    private const val HANGUL_LAST = 0xD7A3
    private const val JUNGSUNG_COUNT = 21
    private const val JONGSUNG_COUNT = 28
    private val CHOSUNG_LIST =
        charArrayOf(
            'ㄱ',
            'ㄲ',
            'ㄴ',
            'ㄷ',
            'ㄸ',
            'ㄹ',
            'ㅁ',
            'ㅂ',
            'ㅃ',
            'ㅅ',
            'ㅆ',
            'ㅇ',
            'ㅈ',
            'ㅉ',
            'ㅊ',
            'ㅋ',
            'ㅌ',
            'ㅍ',
            'ㅎ',
        )
    private val CHOSUNG_ONLY_PATTERN = Regex("^[ㄱ-ㅎ]+$")

    fun String.isChosungOnly(): Boolean = CHOSUNG_ONLY_PATTERN.matches(this)

    // 완성형 한글 음절(가~힣)만 초성으로 치환하고, 그 외 문자(숫자/영문/공백)는 그대로 둔다.
    fun String.extractChosung(): String =
        this
            .map { ch ->
                val code = ch.code
                if (code in HANGUL_BASE..HANGUL_LAST) {
                    CHOSUNG_LIST[(code - HANGUL_BASE) / (JUNGSUNG_COUNT * JONGSUNG_COUNT)]
                } else {
                    ch
                }
            }.joinToString("")
}
