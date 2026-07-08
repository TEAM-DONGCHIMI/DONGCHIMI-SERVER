package kr.dongchimi.core.market

import org.springframework.stereotype.Component
import java.util.Base64

@Component
class FlyerQrManager(
    private val qrCodeGenerator: QrCodeGenerator,
) {
    fun generate(slug: String): String {
        val png = qrCodeGenerator.generate(slug)
        val base64 = Base64.getEncoder().encodeToString(png)
        return "$DATA_URI_PREFIX$base64"
    }

    companion object {
        private const val DATA_URI_PREFIX = "data:image/png;base64,"
    }
}
