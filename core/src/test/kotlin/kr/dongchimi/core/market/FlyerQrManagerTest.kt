package kr.dongchimi.core.market

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import java.util.Base64

class FlyerQrManagerTest :
    FunSpec({
        test("포트가 반환한 PNG 바이트를 Base64 data URI로 인코딩한다") {
            val png = byteArrayOf(1, 2, 3, 4)
            val manager =
                FlyerQrManager(
                    qrCodeGenerator =
                        object : QrCodeGenerator {
                            override fun generate(slug: String): ByteArray = png
                        },
                )

            val result = manager.generate("gangnam-mart")

            result shouldStartWith "data:image/png;base64,"
            result shouldBe "data:image/png;base64,${Base64.getEncoder().encodeToString(png)}"
        }
    })
