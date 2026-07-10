package kr.dongchimi.client.kakao

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.OAuthUserClient
import kr.dongchimi.core.auth.SocialUserInfo
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.Gender
import kr.dongchimi.core.user.SocialAccount
import kr.dongchimi.core.user.SocialProvider
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

private val logger = KotlinLogging.logger {}

@Component
class KakaoUserClient(
    private val kakaoProperties: KakaoProperties,
    private val restClient: RestClient,
) : OAuthUserClient {
    override val provider: SocialProvider = SocialProvider.KAKAO

    override fun fetchUserInfo(code: String): SocialUserInfo {
        val accessToken = fetchAccessToken(code)
        val response = fetchKakaoUser(accessToken)

        val account = SocialAccount(SocialProvider.KAKAO, response.id.toString())
        val kakaoAccount = response.kakaoAccount

        return SocialUserInfo(
            account = account,
            email = kakaoAccount?.email,
            name = kakaoAccount?.profile?.nickname,
            gender = kakaoAccount?.gender?.let(::toGender),
            age = null,
        )
    }

    private fun fetchAccessToken(code: String): String {
        val formBody =
            LinkedMultiValueMap<String, String>().apply {
                add("grant_type", "authorization_code")
                add("client_id", kakaoProperties.clientId)
                add("redirect_uri", kakaoProperties.redirectUri)
                add("code", code)
                if (kakaoProperties.clientSecret.isNotBlank()) {
                    add("client_secret", kakaoProperties.clientSecret)
                }
            }

        val response =
            try {
                restClient
                    .post()
                    .uri(kakaoProperties.tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formBody)
                    .retrieve()
                    .body(KakaoTokenResponse::class.java)
                    ?: throw CoreException(AuthErrorCode.OAUTH_AUTHENTICATION_FAILED)
            } catch (exception: RestClientException) {
                logger.warn(exception) { "카카오 토큰 발급 실패" }
                throw CoreException(AuthErrorCode.OAUTH_AUTHENTICATION_FAILED)
            }

        return response.accessToken
    }

    private fun fetchKakaoUser(accessToken: String): KakaoUserResponse =
        try {
            restClient
                .get()
                .uri(kakaoProperties.userInfoUri)
                .header("Authorization", "Bearer $accessToken")
                .retrieve()
                .body(KakaoUserResponse::class.java)
                ?: throw CoreException(AuthErrorCode.OAUTH_AUTHENTICATION_FAILED)
        } catch (exception: RestClientException) {
            logger.warn(exception) { "카카오 사용자 정보 조회 실패" }
            throw CoreException(AuthErrorCode.OAUTH_AUTHENTICATION_FAILED)
        }

    private fun toGender(kakaoGender: String): Gender? =
        when (kakaoGender) {
            "male" -> Gender.M
            "female" -> Gender.F
            else -> null
        }
}
