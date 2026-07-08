package kr.dongchimi.client.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kr.dongchimi.core.market.QrCodeGenerator
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream

@Component
class ZxingQrCodeGenerator(
    private val qrProperties: QrProperties,
) : QrCodeGenerator {
    override fun generate(slug: String): ByteArray {
        val content = "${qrProperties.baseUrl.trimEnd('/')}/$slug"
        val hints =
            mapOf(
                EncodeHintType.CHARACTER_SET to "UTF-8",
                EncodeHintType.MARGIN to qrProperties.margin,
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            )
        val matrix =
            QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                qrProperties.size,
                qrProperties.size,
                hints,
            )
        return ByteArrayOutputStream().use { out ->
            MatrixToImageWriter.writeToStream(matrix, "PNG", out)
            out.toByteArray()
        }
    }
}
