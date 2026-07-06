package kr.dongchimi.api.owner.config

import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OwnerSwaggerConfig {
    @Bean
    fun ownerOpenApi(): GroupedOpenApi =
        GroupedOpenApi
            .builder()
            .group("owner")
            .displayName("점주 API")
            .packagesToScan("kr.dongchimi.api.owner")
            .build()
}
