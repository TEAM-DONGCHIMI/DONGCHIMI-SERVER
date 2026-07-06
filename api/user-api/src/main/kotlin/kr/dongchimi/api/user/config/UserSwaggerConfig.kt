package kr.dongchimi.api.user.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UserSwaggerConfig {
    @Bean
    fun userOpenApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("user")
            .displayName("사용자 API")
            .packagesToScan("kr.dongchimi.api.user")
            .build()
}
