package kr.dongchimi.core.market

interface QrCodeGenerator {
    fun generate(slug: String): ByteArray
}
