package kr.dongchimi.core.product

import kr.dongchimi.core.product.importjob.ParsedProductRow
import org.springframework.stereotype.Component

@Component
class DraftFailReasonResolver {
    companion object {
        private const val NAME_MAX_LENGTH = 15
        private const val PROMOTIONAL_PHRASE_MAX_LENGTH = 30
    }

    fun resolve(commands: List<PreparedProductDraftSaveCommand>): Map<Long, DraftFailReason?> =
        commands.associate {
            it.id to
                resolve(
                    thumbnailUrl = it.thumbnailUrl,
                    category = it.category,
                    name = it.name,
                    price = it.price,
                    discountPeriod = it.discountPeriod,
                    promotionalPhrase = it.promotionalPhrase,
                )
        }

    /**
     * 엑셀 분석 워커가 만든 초안(아직 id가 없는 신규 행)의 판정에 쓴다. [PreparedProductDraftSaveCommand]
     * 기반 판정과 같은 기준(아래 resolve)을 공유해 판정 지점을 하나로 유지한다.
     */
    fun resolve(
        row: ParsedProductRow,
        category: ProductCategory?,
        thumbnailUrl: String?,
    ): DraftFailReason? =
        resolve(
            thumbnailUrl = thumbnailUrl,
            category = category,
            name = row.name,
            price = row.price,
            discountPeriod = row.discountPeriod,
            row.promotionalPhrase,
        )

    /**
     * [Product]가 non-null로 요구하는 필드 중 초안에서 비어 있을 수 있는 것들을 검사하고,
     * 값이 있어도 유효하지 않은 경우(길이 초과 등)까지 함께 검사한다.
     * null을 반환하면 상품으로 등록 가능한 상태다.
     *
     * dealType은 기본값이 있어 비어 있을 수 없으므로 검사하지 않는다.
     */
    private fun resolve(
        thumbnailUrl: String?,
        category: ProductCategory?,
        name: String?,
        price: Price?,
        discountPeriod: DiscountPeriod?,
        promotionalPhrase: String?,
    ): DraftFailReason? =
        when {
            thumbnailUrl == null -> DraftFailReason.THUMBNAIL_MISSING
            category == null -> DraftFailReason.CATEGORY_MISSING
            name == null -> DraftFailReason.NAME_MISSING
            name.length > NAME_MAX_LENGTH -> DraftFailReason.NAME_INVALID
            price == null -> DraftFailReason.PRICE_MISSING
            discountPeriod == null -> DraftFailReason.DISCOUNT_PERIOD_MISSING
            promotionalPhrase != null && promotionalPhrase.length > PROMOTIONAL_PHRASE_MAX_LENGTH -> DraftFailReason.PHRASE_INVALID
            else -> null
        }
}
