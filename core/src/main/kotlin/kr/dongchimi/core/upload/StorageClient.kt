package kr.dongchimi.core.upload

interface StorageClient {
    fun createUploadUrl(
        objectKey: String,
        contentType: String,
        contentLength: Long,
    ): PresignedUpload

    fun getObjectMetadata(objectKey: String): StoredObjectMetadata?

    fun moveObject(
        sourceKey: String,
        destinationKey: String,
    )

    fun resolveAccessUrl(objectKey: String): String
}
