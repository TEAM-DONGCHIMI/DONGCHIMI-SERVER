package kr.dongchimi.bootstrap.config

import kr.dongchimi.core.product.importjob.ImportJobProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

/**
 * core의 [ImportJobProperties]를 `import.job.*`에 바인딩하는 구현체.
 * 소비처(worker/service)는 core에 있으나 core를 spring-free로 두기 위해 바인딩만 bootstrap이 소유한다.
 */
@ConfigurationProperties(prefix = "import.job")
data class ImportJobPropertiesBinding(
    override val lease: Duration,
    override val pollInterval: Duration,
    override val slotsPerInstance: Int,
    override val maxAttempts: Int,
    override val aiConcurrency: Int,
    override val aiBatchSize: Int,
) : ImportJobProperties
