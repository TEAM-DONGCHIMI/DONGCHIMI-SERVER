package kr.dongchimi.core.upload

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.UUID

@Component
class ObjectKeyGenerator {
    fun generateTempKey(
        purpose: UploadPurpose,
        contentType: String,
    ): String {
        val ext = extensionOf(contentType)
        return "$TEMP_PREFIX/${purpose.name}/${UUID.randomUUID()}.$ext"
    }

    fun parsePurpose(tempKey: String): UploadPurpose {
        val parts = tempKey.split("/")
        if (parts.size != 3 || parts[0] != TEMP_PREFIX) {
            throw CoreException(UploadErrorCode.INVALID_UPLOAD_KEY)
        }
        return runCatching { UploadPurpose.valueOf(parts[1]) }
            .getOrElse { throw CoreException(UploadErrorCode.INVALID_UPLOAD_KEY) }
    }

    fun toPermanentKey(tempKey: String): String {
        val purpose = parsePurpose(tempKey)
        val fileName = tempKey.substringAfterLast("/")
        val now = LocalDate.now()
        return "${purpose.prefix}/%04d/%02d/$fileName".format(now.year, now.monthValue)
    }

    private fun extensionOf(contentType: String): String =
        EXTENSIONS_BY_CONTENT_TYPE[contentType]
            ?: throw CoreException(UploadErrorCode.UNSUPPORTED_CONTENT_TYPE)

    companion object {
        const val TEMP_PREFIX = "tmp"
        private val EXTENSIONS_BY_CONTENT_TYPE =
            mapOf(
                "image/jpeg" to "jpg",
                "image/png" to "png",
                "image/webp" to "webp",
            )
    }
}
