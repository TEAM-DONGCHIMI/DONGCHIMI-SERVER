package kr.dongchimi.client.kakao

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoUserResponse(
    val id: Long,
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount?,
) {
    data class KakaoAccount(
        val email: String?,
        val profile: Profile?,
        val gender: String?,
        @JsonProperty("age_range")
        val ageRange: String?,
    ) {
        data class Profile(
            val nickname: String?,
        )
    }
}
