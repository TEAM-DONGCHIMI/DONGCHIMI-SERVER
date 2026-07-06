package kr.dongchimi.core.upload

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class UploadContentTypeValidator {
    fun validate(
        purpose: UploadPurpose,
        contentType: String,
    ) {
        if (contentType !in purpose.allowedContentTypes) {
            throw CoreException(UploadErrorCode.UNSUPPORTED_CONTENT_TYPE)
        }
    }
}
