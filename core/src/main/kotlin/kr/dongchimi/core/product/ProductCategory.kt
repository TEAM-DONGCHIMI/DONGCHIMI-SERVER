package kr.dongchimi.core.product

enum class ProductCategory(
    val displayName: String,
) {
    VEGETABLE_FRUIT("채소/과일"),
    MEAT_EGG("정육/달걀"),
    SEAFOOD("수산물"),
    DAIRY("유제품"),
    CONVENIENCE_FOOD("간편식"),
    PROCESSED_FOOD("가공식품"),
    BEVERAGE_ALCOHOL("음료/주류"),
    HOUSEHOLD_GOODS("생활용품"),
    ETC("기타"),
}
