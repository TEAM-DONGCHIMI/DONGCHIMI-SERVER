package kr.dongchimi.core.product

/**
 * 선언 순서가 우선순위다. 여러 항목이 동시에 누락되면 먼저 선언된 사유 하나만 사용한다.
 */
enum class DraftFailReason(
    val displayName: String,
) {
    THUMBNAIL_MISSING("이미지 누락"),
    CATEGORY_MISSING("카테고리 미선택"),
    NAME_MISSING("상품명 미입력"),
    PRICE_MISSING("판매가격 미입력"),
    DISCOUNT_PERIOD_MISSING("할인기간 미설정"),
    NAME_INVALID("상품명 확인 필요"),
    PHRASE_INVALID("홍보문구 확인 필요"),
}
