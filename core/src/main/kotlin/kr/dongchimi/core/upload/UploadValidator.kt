package kr.dongchimi.core.upload

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class UploadValidator(
    private val uploadProperties: UploadProperties,
) {
    fun validate(
        purpose: UploadPurpose,
        contentType: String,
        contentLength: Long,
    ) {
        if (contentType !in purpose.allowedContentTypes) {
            throw CoreException(UploadErrorCode.UNSUPPORTED_CONTENT_TYPE)
        }

        if (contentLength > uploadProperties.maxSizeBytes.getValue(purpose)) {
            throw CoreException(UploadErrorCode.FILE_TOO_LARGE)
        }
    }
}
