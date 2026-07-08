package kr.dongchimi.infrastructure.qr

import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

class ZxingQrCodeGeneratorTest :
    FunSpec({
        fun generator(
            baseUrl: String = "https://app.dongchiimi.com",
            size: Int = 183,
            margin: Int = 1,
        ) = ZxingQrCodeGenerator(QrProperties(baseUrl = baseUrl, size = size, margin = margin))

        test("PNG 매직넘버로 시작하는 유효한 이미지를 생성한다") {
            val png = generator().generate("gangnam-mart")

            png.size shouldBeGreaterThan 8
            png.copyOfRange(0, 8) shouldBe byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)
        }

        test("설정한 size와 동일한 픽셀 크기의 이미지를 생성한다") {
            val png = generator(size = 183).generate("gangnam-mart")

            val image = ImageIO.read(ByteArrayInputStream(png))

            image.width shouldBe 183
            image.height shouldBe 183
        }

        test("생성한 QR코드를 디코딩하면 base-url과 slug를 조합한 원본 URL이 나온다") {
            val png = generator(baseUrl = "https://app.dongchiimi.com", size = 300, margin = 1).generate("gangnam-mart")

            val decoded = decode(png)

            decoded shouldBe "https://app.dongchiimi.com/gangnam-mart"
        }

        test("base-url 끝에 슬래시가 있어도 // 없이 조립한다") {
            val png = generator(baseUrl = "https://app.dongchiimi.com/", size = 300, margin = 1).generate("gangnam-mart")

            val decoded = decode(png)

            decoded shouldBe "https://app.dongchiimi.com/gangnam-mart"
        }
    })

private fun decode(png: ByteArray): String {
    val image = ImageIO.read(ByteArrayInputStream(png))
    val bitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(image)))
    return QRCodeReader().decode(bitmap).text
}
