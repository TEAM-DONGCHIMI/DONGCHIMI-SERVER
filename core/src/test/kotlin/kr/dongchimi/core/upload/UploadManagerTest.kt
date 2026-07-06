package kr.dongchimi.core.upload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.common.exception.CoreException
import java.time.Instant

private val TEST_PROPERTIES =
    UploadProperties(
        maxSizeBytes =
            mapOf(
                UploadPurpose.PRODUCT_THUMBNAIL to 5 * 1024 * 1024L,
                UploadPurpose.DEFAULT_PRODUCT_THUMBNAIL to 5 * 1024 * 1024L,
            ),
    )

class UploadManagerTest :
    FunSpec({
        fun manager(storageClient: StorageClient) =
            UploadManager(storageClient, ObjectKeyGenerator(), UploadContentTypeValidator(), TEST_PROPERTIES)

        test("발급 요청이 유효하면 presigned URL을 반환한다") {
            val storageClient = FakeStorageClient()

            val result = manager(storageClient).issue(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg", 1024L)

            result.uploadUrl shouldBe "https://s3.example.com/${result.objectKey}"
        }

        test("허용되지 않은 content-type이면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    manager(FakeStorageClient()).issue(UploadPurpose.PRODUCT_THUMBNAIL, "application/pdf", 1024L)
                }

            exception.errorCode shouldBe UploadErrorCode.UNSUPPORTED_CONTENT_TYPE
        }

        test("최대 크기를 초과하면 예외가 발생한다") {
            val exception =
                shouldThrow<CoreException> {
                    manager(FakeStorageClient()).issue(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg", 10 * 1024 * 1024L)
                }

            exception.errorCode shouldBe UploadErrorCode.FILE_TOO_LARGE
        }

        test("승격에 성공하면 정식 경로와 accessUrl을 반환한다") {
            val storageClient = FakeStorageClient()
            val tempKey = ObjectKeyGenerator().generateTempKey(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg")
            storageClient.put(tempKey, StoredObjectMetadata("image/jpeg", 1024L))

            val promoted = manager(storageClient).promote(tempKey)

            promoted.objectKey shouldBe ObjectKeyGenerator().toPermanentKey(tempKey)
            promoted.accessUrl shouldBe "https://cdn.example.com/${promoted.objectKey}"
        }

        test("업로드되지 않은 key를 승격하면 예외가 발생한다") {
            val tempKey = ObjectKeyGenerator().generateTempKey(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg")

            val exception =
                shouldThrow<CoreException> {
                    manager(FakeStorageClient()).promote(tempKey)
                }

            exception.errorCode shouldBe UploadErrorCode.UPLOAD_NOT_FOUND
        }

        test("승격 시점에 실제 크기가 초과하면 예외가 발생한다") {
            val storageClient = FakeStorageClient()
            val tempKey = ObjectKeyGenerator().generateTempKey(UploadPurpose.PRODUCT_THUMBNAIL, "image/jpeg")
            storageClient.put(tempKey, StoredObjectMetadata("image/jpeg", 10 * 1024 * 1024L))

            val exception =
                shouldThrow<CoreException> {
                    manager(storageClient).promote(tempKey)
                }

            exception.errorCode shouldBe UploadErrorCode.FILE_TOO_LARGE
        }
    }) {
    private class FakeStorageClient : StorageClient {
        private val store = mutableMapOf<String, StoredObjectMetadata>()

        fun put(
            objectKey: String,
            metadata: StoredObjectMetadata,
        ) {
            store[objectKey] = metadata
        }

        override fun createUploadUrl(
            objectKey: String,
            contentType: String,
            contentLength: Long,
        ): PresignedUpload =
            PresignedUpload(
                uploadUrl = "https://s3.example.com/$objectKey",
                objectKey = objectKey,
                expiresAt = Instant.now(),
            )

        override fun getObjectMetadata(objectKey: String): StoredObjectMetadata? = store[objectKey]

        override fun moveObject(
            sourceKey: String,
            destinationKey: String,
        ) {
            val metadata = store.remove(sourceKey) ?: return
            store[destinationKey] = metadata
        }

        override fun resolveAccessUrl(objectKey: String): String = "https://cdn.example.com/$objectKey"
    }
}
