package kr.dongchimi.core.product.importjob

interface ExcelProductParser {
    fun parse(bytes: ByteArray): List<ParsedProductRow>
}
