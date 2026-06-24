package kr.dongchimi.db.common.config

import org.springframework.boot.persistence.autoconfigure.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan(basePackages = ["kr.dongchimi.db"])
@EnableJpaRepositories(basePackages = ["kr.dongchimi.db"])
class JpaConfig {
}