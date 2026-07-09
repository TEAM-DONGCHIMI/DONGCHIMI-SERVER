package kr.dongchimi.infrastructure.excel

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.time.LocalDate

/**
 * docs/api/excel_ref.xlsx 원본 양식은 1~5행(제목·안내·예시)만 있고 데이터 행이 없다.
 * 실제 양식 파일을 그대로 불러와 6행부터 테스트 데이터를 얹어서, 데이터 시작 위치가
 * 파서의 하드코딩된 상수가 아니라 실제 양식 구조와 일치하는지 함께 검증한다.
 */
class PoiExcelProductParserTest :
    FunSpec({
        val parser = PoiExcelProductParser()

        fun buildWorkbook(rows: List<List<String?>>): ByteArray {
            val template = requireNotNull(ClassLoader.getSystemResourceAsStream("excel_ref.xlsx")) { "excel_ref.xlsx fixture not found" }
            WorkbookFactory.create(template).use { workbook ->
                val sheet = workbook.getSheetAt(0)
                rows.forEachIndexed { rowOffset, cells ->
                    val row = sheet.createRow(5 + rowOffset)
                    cells.forEachIndexed { columnIndex, value ->
                        if (value != null) row.createCell(columnIndex).setCellValue(value)
                    }
                }
                val output = ByteArrayOutputStream()
                workbook.write(output)
                return output.toByteArray()
            }
        }

        test("6행부터 읽고, 양식의 5행 이전(제목·안내·예시)은 데이터로 취급하지 않는다") {
            val bytes = buildWorkbook(listOf(listOf("콩나물", "1350", "2026.07.09 ~ 2026.07.13", "아삭한 식감")))

            val rows = parser.parse(bytes)

            rows shouldHaveSize 1
            rows[0].name shouldBe "콩나물"
        }

        test("판매가만 파싱하고 원가는 판매가와 동일하게 채운다") {
            val bytes = buildWorkbook(listOf(listOf("콩나물", "1350", "2026.07.09 ~ 2026.07.13", null)))

            val price = parser.parse(bytes).single().price

            price.shouldNotBeNull()
            price.originalPrice shouldBe BigDecimal("1350")
            price.discountedPrice shouldBe BigDecimal("1350")
        }

        test("판매가가 숫자 타입 셀이어도 파싱한다") {
            val template = requireNotNull(ClassLoader.getSystemResourceAsStream("excel_ref.xlsx"))
            val bytes =
                WorkbookFactory.create(template).use { workbook ->
                    val sheet = workbook.getSheetAt(0)
                    val row = sheet.createRow(5)
                    row.createCell(0).setCellValue("콩나물")
                    row.createCell(1).setCellValue(1350.0)
                    val output = ByteArrayOutputStream()
                    workbook.write(output)
                    output.toByteArray()
                }

            val price = parser.parse(bytes).single().price

            price.shouldNotBeNull()
            price.originalPrice shouldBe BigDecimal("1350")
        }

        test("할인기간을 시작일~종료일로 파싱한다") {
            val bytes = buildWorkbook(listOf(listOf("콩나물", "1350", "2026.07.09 ~ 2026.07.13", null)))

            val period = parser.parse(bytes).single().discountPeriod

            period.shouldNotBeNull()
            period.discountStartDate shouldBe LocalDate.of(2026, 7, 9)
            period.discountEndDate shouldBe LocalDate.of(2026, 7, 13)
        }

        test("전 컬럼이 빈 행은 건너뛴다") {
            val bytes =
                buildWorkbook(
                    listOf(
                        listOf("콩나물", "1350", "2026.07.09 ~ 2026.07.13", null),
                        listOf(null, null, null, null),
                        listOf("두부", "2000", "2026.07.09 ~ 2026.07.13", null),
                    ),
                )

            parser.parse(bytes) shouldHaveSize 2
        }

        test("할인기간이 누락되면 discountPeriod는 null이고 나머지 필드는 그대로 파싱된다 — 파서는 failReason을 만들지 않는다") {
            val bytes = buildWorkbook(listOf(listOf("콩나물", "1350", null, null)))

            val row = parser.parse(bytes).single()

            row.name shouldBe "콩나물"
            row.price.shouldNotBeNull()
            row.discountPeriod.shouldBeNull()
        }

        test("홍보문구는 선택 항목이라 비어 있으면 null이다") {
            val bytes = buildWorkbook(listOf(listOf("콩나물", "1350", "2026.07.09 ~ 2026.07.13", null)))

            parser
                .parse(bytes)
                .single()
                .promotionalPhrase
                .shouldBeNull()
        }
    })
