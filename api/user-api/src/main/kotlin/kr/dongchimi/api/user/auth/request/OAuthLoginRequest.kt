package kr.dongchimi.api.user.auth.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.api.core.exception.validate
import kr.dongchimi.core.auth.AuthErrorCode
import kr.dongchimi.core.auth.OAuthLoginCommand
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.user.SocialProvider

data class OAuthLoginRequest(
    @Schema(description = "소셜 제공자로부터 발급받은 access token")
    val accessToken: String,
) {
    fun toCommand(provider: String): OAuthLoginCommand {
        validate(accessToken.isNotBlank()) { "액세스 토큰은 필수로 입력해 주세요." }

        val socialProvider =
            runCatching { SocialProvider.valueOf(provider.uppercase()) }
                .getOrElse { throw CoreException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER) }

        return OAuthLoginCommand(socialProvider, accessToken)
    }
}
