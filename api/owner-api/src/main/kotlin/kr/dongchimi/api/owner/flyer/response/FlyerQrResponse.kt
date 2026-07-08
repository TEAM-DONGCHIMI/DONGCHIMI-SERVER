package kr.dongchimi.api.owner.flyer.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.market.FlyerQr

data class FlyerQrResponse(
    @Schema(description = "QR코드 이미지 (Base64 인코딩)")
    val qrCode: String,
) {
    constructor(flyerQr: FlyerQr) : this(flyerQr.qrCode)
}
