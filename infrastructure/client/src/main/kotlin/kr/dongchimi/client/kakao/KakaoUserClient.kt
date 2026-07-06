package kr.dongchimi.client.kakao

import io.github.oshai.kotlinlogging.KotlinLogging
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.OAuthUserClient
import kr.dongchimi.core.auth.SocialUserInfo
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.Gender
import kr.dongchimi.core.user.SocialAccount
import kr.dongchimi.core.user.SocialProvider
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException

private val logger = KotlinLogging.logger {}

@Component
class KakaoUserClient(
    private val kakaoProperties: KakaoProperties,
    private val restClient: RestClient,
) : OAuthUserClient {
    override val provider: SocialProvider = SocialProvider.KAKAO

    override fun fetchUserInfo(accessToken: String): SocialUserInfo {
        val response =
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

    private fun toGender(kakaoGender: String): Gender? =
        when (kakaoGender) {
            "male" -> Gender.M
            "female" -> Gender.F
            else -> null
        }
}
