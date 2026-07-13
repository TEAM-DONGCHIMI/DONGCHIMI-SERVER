package kr.dongchimi.gateway.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "admin.signup")
data class AdminSignupProperties(
    val verificationCode: String,
)
