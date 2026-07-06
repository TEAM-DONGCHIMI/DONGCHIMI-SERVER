package kr.dongchimi.api.admin.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdminSwaggerConfig {
    @Bean
    fun adminOpenApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("admin")
            .displayName("관리자 API")
            .packagesToScan("kr.dongchimi.api.admin")
            .build()
}
