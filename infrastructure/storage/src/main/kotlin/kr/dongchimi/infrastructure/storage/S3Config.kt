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
    private val props: S3Properties,
) {
    @Bean
    fun s3Client(): S3Client =
        S3Client
            .builder()
            .region(Region.of(props.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .apply { props.endpoint?.takeIf { it.isNotBlank() }?.let { endpointOverride(URI.create(it)) } }
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(props.pathStyleAccess).build())
            .build()

    @Bean
    fun s3Presigner(): S3Presigner =
        S3Presigner
            .builder()
            .region(Region.of(props.region))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .apply { props.endpoint?.takeIf { it.isNotBlank() }?.let { endpointOverride(URI.create(it)) } }
            .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(props.pathStyleAccess).build())
            .build()
}
