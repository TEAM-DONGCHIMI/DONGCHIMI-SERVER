package kr.dongchimi.client.excel

import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price
import kr.dongchimi.core.product.importjob.ExcelProductParser
import kr.dongchimi.core.product.importjob.ParsedProductRow
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * docs/api/excel_ref.xlsx 양식 전용 파서. 1~5행은 제목·안내·예시이고 데이터는 6행부터다
 * (POI는 0-based 행 인덱스를 쓰므로 시작 인덱스는 5).
 */
@Component
class PoiExcelProductParser : ExcelProductParser {
    private val dataFormatter = DataFormatter()

    override fun parse(bytes: ByteArray): List<ParsedProductRow> =
        WorkbookFactory.create(ByteArrayInputStream(bytes)).use { workbook ->
            val sheet = workbook.getSheetAt(0)

            (DATA_START_ROW_INDEX..sheet.lastRowNum)
                .mapNotNull { sheet.getRow(it) }
                .filterNot(::isBlankRow)
                .map(::parseRow)
        }

    private fun isBlankRow(row: Row): Boolean = COLUMN_INDICES.all { cellText(row, it).isBlank() }

    private fun parseRow(row: Row): ParsedProductRow =
        ParsedProductRow(
            name = cellText(row, COLUMN_NAME).ifBlank { null },
            price = parsePrice(cellText(row, COLUMN_PRICE)),
            discountPeriod = parseDiscountPeriod(cellText(row, COLUMN_DISCOUNT_PERIOD)),
            promotionalPhrase = cellText(row, COLUMN_PROMOTIONAL_PHRASE).ifBlank { null },
        )

    private fun cellText(
        row: Row,
        columnIndex: Int,
    ): String {
        val cell = row.getCell(columnIndex) ?: return ""
        return dataFormatter.formatCellValue(cell).trim()
    }

    /**
     * 양식에 원가 컬럼이 없어 판매가만 파싱하고, 원가는 판매가와 동일하게 채운다
     * (docs/plans/product-excel-import-analysis-plan.md #7).
     */
    private fun parsePrice(text: String): Price? {
        val amount =
            text
                .replace(",", "")
                .replace("원", "")
                .trim()
                .toBigDecimalOrNull() ?: return null
        return Price(originalPrice = amount, discountedPrice = amount)
    }

    private fun parseDiscountPeriod(text: String): DiscountPeriod? {
        val parts = text.split("~").map { it.trim() }
        if (parts.size != 2) return null

        return try {
            DiscountPeriod(
                discountStartDate = LocalDate.parse(parts[0], DATE_FORMATTER),
                discountEndDate = LocalDate.parse(parts[1], DATE_FORMATTER),
            )
        } catch (e: DateTimeParseException) {
            null
        }
    }

    companion object {
        private const val DATA_START_ROW_INDEX = 5
        private const val COLUMN_NAME = 0
        private const val COLUMN_PRICE = 1
        private const val COLUMN_DISCOUNT_PERIOD = 2
        private const val COLUMN_PROMOTIONAL_PHRASE = 3
        private val COLUMN_INDICES = listOf(COLUMN_NAME, COLUMN_PRICE, COLUMN_DISCOUNT_PERIOD, COLUMN_PROMOTIONAL_PHRASE)
        private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd")
    }
}
