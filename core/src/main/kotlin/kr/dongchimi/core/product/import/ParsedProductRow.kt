package kr.dongchimi.core.product.import

import kr.dongchimi.core.product.DiscountPeriod
import kr.dongchimi.core.product.Price

/**
 * 엑셀 한 행을 파싱한 결과. 필드별로 실패할 수 있어 전부 nullable이며,
 * failReason 판정은 [kr.dongchimi.core.product.DraftFailReasonResolver]가 한 곳에서 담당한다 — 파서는 판정하지 않는다.
 */
data class ParsedProductRow(
    val name: String?,
    val price: Price?,
    val discountPeriod: DiscountPeriod?,
    val promotionalPhrase: String?,
)
