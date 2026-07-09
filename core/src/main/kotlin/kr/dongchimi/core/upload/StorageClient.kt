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

    /**
     * accessUrl이 우리 버킷(cdnBaseUrl) 소속이면 objectKey로 역변환하고, 아니면 null을 반환한다.
     * 임의 URL을 그대로 fetch하면 SSRF가 되므로, 다운로드는 반드시 이 objectKey를 거쳐서만 한다.
     */
    fun resolveObjectKey(accessUrl: String): String?

    fun download(objectKey: String): ByteArray
}
