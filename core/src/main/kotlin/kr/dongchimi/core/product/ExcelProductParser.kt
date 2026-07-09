package kr.dongchimi.core.product

interface ExcelProductParser {
    fun parse(bytes: ByteArray): List<ParsedProductRow>
}
