package kr.dongchimi.infrastructure.storage

import kr.dongchimi.core.upload.PresignedUpload
import kr.dongchimi.core.upload.StorageClient
import kr.dongchimi.core.upload.StoredObjectMetadata
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.CopyObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Instant

@Component
@ConditionalOnProperty(name = ["storage.provider"], havingValue = "s3", matchIfMissing = true)
class S3StorageClient(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    private val storageProps: StorageProperties,
    private val s3Props: S3Properties,
) : StorageClient {
    override fun createUploadUrl(
        objectKey: String,
        contentType: String,
        contentLength: Long,
    ): PresignedUpload {
        val putRequest =
            PutObjectRequest
                .builder()
                .bucket(s3Props.bucket)
                .key(objectKey)
                .contentType(contentType)
                .contentLength(contentLength)
                .build()
        val presignRequest =
            PutObjectPresignRequest
                .builder()
                .signatureDuration(storageProps.presignExpiry)
                .putObjectRequest(putRequest)
                .build()
        val presigned = s3Presigner.presignPutObject(presignRequest)

        return PresignedUpload(
            uploadUrl = presigned.url().toString(),
            objectKey = objectKey,
            expiresAt = Instant.now().plus(storageProps.presignExpiry),
            requiredHeaders = mapOf("Content-Type" to contentType),
        )
    }

    override fun getObjectMetadata(objectKey: String): StoredObjectMetadata? =
        try {
            val head =
                s3Client.headObject(
                    HeadObjectRequest
                        .builder()
                        .bucket(s3Props.bucket)
                        .key(objectKey)
                        .build(),
                )
            StoredObjectMetadata(head.contentType(), head.contentLength())
        } catch (e: NoSuchKeyException) {
            null
        }

    override fun moveObject(
        sourceKey: String,
        destinationKey: String,
    ) {
        s3Client.copyObject(
            CopyObjectRequest
                .builder()
                .sourceBucket(s3Props.bucket)
                .sourceKey(sourceKey)
                .destinationBucket(s3Props.bucket)
                .destinationKey(destinationKey)
                .build(),
        )
        s3Client.deleteObject(
            DeleteObjectRequest
                .builder()
                .bucket(s3Props.bucket)
                .key(sourceKey)
                .build(),
        )
    }

    override fun resolveAccessUrl(objectKey: String): String = "${storageProps.cdnBaseUrl}/$objectKey"
}
