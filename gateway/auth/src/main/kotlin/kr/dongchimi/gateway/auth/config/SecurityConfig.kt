package kr.dongchimi.gateway.auth.config

import kr.dongchimi.core.auth.Role
import kr.dongchimi.gateway.auth.PublicEndpoints
import kr.dongchimi.gateway.auth.jwt.JwtAuthFilter
import kr.dongchimi.gateway.auth.jwt.JwtProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtProvider: JwtProvider,
    private val corsProperties: CorsProperties,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            cors { }
            httpBasic { disable() }
            formLogin { disable() }
            logout { disable() }
            csrf { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            authorizeHttpRequests {
                PublicEndpoints.SWAGGER.forEach { authorize(it, permitAll) }
                authorize(OWNER_API_PATTERN, hasAuthority(Role.OWNER.name))
                authorize(ADMIN_API_PATTERN, hasAuthority(Role.ADMIN.name))
                authorize(USER_API_PATTERN, hasAuthority(Role.USER.name))
                authorize(anyRequest, authenticated)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(JwtAuthFilter(jwtProvider))
        }

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config =
            CorsConfiguration().apply {
                allowedOrigins = corsProperties.allowedOrigins
                allowedMethods = corsProperties.allowedMethods
                allowedHeaders = corsProperties.allowedHeaders
                allowCredentials = true
            }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }

    companion object {
        const val OWNER_API_PATTERN = "/v1/owners/**"
        const val ADMIN_API_PATTERN = "/v1/admin/**"
        const val USER_API_PATTERN = "/v1/users/**"
    }
}
