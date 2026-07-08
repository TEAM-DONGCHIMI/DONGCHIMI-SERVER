package kr.dongchimi.client.qr

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "qr")
data class QrProperties(
    val baseUrl: String, // 전단 공유 페이지 base URL. QR 내용 = {baseUrl}/{slug}
    val size: Int, // px (정사각)
    val margin: Int, // quiet zone 모듈 수
)
