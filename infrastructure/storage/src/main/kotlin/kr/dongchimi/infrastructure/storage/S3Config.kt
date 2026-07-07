package kr.dongchimi.infrastructure.storage

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
@ConditionalOnProperty(name = ["storage.provider"], havingValue = "s3", matchIfMissing = true)
class S3Config(
    private val properties: S3Properties,
) {
    private val endpointOverride: URI? = properties.endpoint?.takeIf { it.isNotBlank() }?.let { URI.create(it) }
    private val s3ServiceConfiguration: S3Configuration =
        S3Configuration.builder().pathStyleAccessEnabled(properties.pathStyleAccess).build()

    @Bean
    fun s3Client(): S3Client =
        S3Client
            .builder()
            .region(Region.of(properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .apply { endpointOverride?.let { endpointOverride(it) } }
            .serviceConfiguration(s3ServiceConfiguration)
            .build()

    @Bean
    fun s3Presigner(): S3Presigner =
        S3Presigner
            .builder()
            .region(Region.of(properties.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .apply { endpointOverride?.let { endpointOverride(it) } }
            .serviceConfiguration(s3ServiceConfiguration)
            .build()
}
