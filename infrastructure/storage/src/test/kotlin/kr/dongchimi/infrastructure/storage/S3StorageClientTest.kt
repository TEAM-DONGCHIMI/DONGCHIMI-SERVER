package kr.dongchimi.infrastructure.storage

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kr.dongchimi.infrastructure.storage.testsupport.TestLocalStackContainer
import org.testcontainers.containers.localstack.LocalStackContainer
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.CreateBucketRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

private const val BUCKET = "dongchimi-storage-test"

class S3StorageClientTest :
    FunSpec({
        val container = TestLocalStackContainer.container
        val endpoint = container.getEndpointOverride(LocalStackContainer.Service.S3)
        val credentialsProvider =
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(container.accessKey, container.secretKey),
            )
        val serviceConfiguration = S3Configuration.builder().pathStyleAccessEnabled(true).build()

        val s3Client =
            S3Client
                .builder()
                .endpointOverride(endpoint)
                .region(Region.of(container.region))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration)
                .build()
        val s3Presigner =
            S3Presigner
                .builder()
                .endpointOverride(endpoint)
                .region(Region.of(container.region))
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(serviceConfiguration)
                .build()

        s3Client.createBucket(CreateBucketRequest.builder().bucket(BUCKET).build())

        val storageClient =
            S3StorageClient(
                s3Client,
                s3Presigner,
                StorageProperties(provider = "s3", presignExpiry = Duration.ofMinutes(5), cdnBaseUrl = "https://cdn.example.com"),
                S3Properties(region = container.region, bucket = BUCKET, endpoint = endpoint.toString(), pathStyleAccess = true),
            )
        val httpClient = HttpClient.newHttpClient()

        fun putViaPresignedUrl(
            uploadUrl: String,
            contentType: String,
            body: ByteArray,
        ): Int {
            val request =
                HttpRequest
                    .newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", contentType)
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build()
            return httpClient.send(request, HttpResponse.BodyHandlers.discarding()).statusCode()
        }

        test("발급된 presigned URL로 업로드하면 HeadObject로 메타데이터를 조회할 수 있다") {
            val tempKey = "tmp/PRODUCT_THUMBNAIL/head-test.jpg"
            val body = "hello-world".toByteArray()

            val presigned = storageClient.createUploadUrl(tempKey, "image/jpeg", body.size.toLong())
            val status = putViaPresignedUrl(presigned.uploadUrl, "image/jpeg", body)

            status shouldBe 200
            val metadata = storageClient.getObjectMetadata(tempKey)
            metadata.shouldNotBeNull()
            metadata.contentType shouldBe "image/jpeg"
            metadata.contentLength shouldBe body.size.toLong()
        }

        test("존재하지 않는 key를 조회하면 null을 반환한다") {
            storageClient.getObjectMetadata("tmp/PRODUCT_THUMBNAIL/missing.jpg").shouldBeNull()
        }

        test("승격(move)하면 정식 경로로 복사되고 tmp 경로는 삭제된다") {
            val tempKey = "tmp/PRODUCT_THUMBNAIL/move-test.jpg"
            val permanentKey = "products/thumbnails/2026/07/move-test.jpg"
            val body = "move-me".toByteArray()
            val presigned = storageClient.createUploadUrl(tempKey, "image/jpeg", body.size.toLong())
            putViaPresignedUrl(presigned.uploadUrl, "image/jpeg", body)

            storageClient.moveObject(tempKey, permanentKey)

            storageClient.getObjectMetadata(tempKey).shouldBeNull()
            storageClient.getObjectMetadata(permanentKey).shouldNotBeNull()
        }

        test("resolveAccessUrl은 cdnBaseUrl과 objectKey를 조합한다") {
            storageClient.resolveAccessUrl("products/thumbnails/2026/07/x.jpg") shouldBe
                "https://cdn.example.com/products/thumbnails/2026/07/x.jpg"
        }

        test("cdnBaseUrl에 trailing slash가 있어도 이중 슬래시가 생기지 않는다") {
            val clientWithTrailingSlash =
                S3StorageClient(
                    s3Client,
                    s3Presigner,
                    StorageProperties(provider = "s3", presignExpiry = Duration.ofMinutes(5), cdnBaseUrl = "https://cdn.example.com/"),
                    S3Properties(region = container.region, bucket = BUCKET, endpoint = endpoint.toString(), pathStyleAccess = true),
                )

            clientWithTrailingSlash.resolveAccessUrl("x.jpg") shouldBe "https://cdn.example.com/x.jpg"
        }
    })
