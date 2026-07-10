package kr.dongchimi.core.product.import

interface ExcelProductParser {
    fun parse(bytes: ByteArray): List<ParsedProductRow>
}
