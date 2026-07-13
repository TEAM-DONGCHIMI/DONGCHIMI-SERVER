package kr.dongchimi.gateway.auth

object PublicEndpoints {
    val SWAGGER =
        arrayOf(
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/swagger",
        )

    val AUTH =
        arrayOf(
            "/v1/users/login/oauth2/**",
            "/v1/auth/token/refresh",
            "/v1/owners/auth/signup",
            "/v1/owners/auth/signup/complete",
            "/v1/owners/auth/login",
            "/v1/admin/signup",
            "/v1/admin/login",
        )

    val LOCAL =
        arrayOf(
            "/v1/auth/local-token",
        )

    val ACTUATOR =
        arrayOf(
            "/actuator/health",
            "/actuator/health/**",
        )
}
