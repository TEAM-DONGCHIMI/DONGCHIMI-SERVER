package kr.dongchimi.core.upload

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class UploadValidator {
    fun validate(
        purpose: UploadPurpose,
        contentType: String,
        contentLength: Long,
    ) {
        if (contentType !in purpose.allowedContentTypes) {
            throw CoreException(UploadErrorCode.UNSUPPORTED_CONTENT_TYPE)
        }

        if (contentLength > purpose.maxSizeBytes) {
            throw CoreException(UploadErrorCode.FILE_TOO_LARGE)
        }
    }
}
