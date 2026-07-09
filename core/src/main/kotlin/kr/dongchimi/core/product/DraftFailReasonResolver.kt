package kr.dongchimi.core.product

import org.springframework.stereotype.Component

@Component
class DraftFailReasonResolver {
    fun resolve(commands: List<PreparedProductDraftSaveCommand>): Map<Long, DraftFailReason?> = commands.associate { it.id to resolve(it) }

    /**
     * [Product]가 non-null로 요구하는 필드 중 초안에서 비어 있을 수 있는 것들을 검사한다.
     * null을 반환하면 상품으로 등록 가능한 상태다.
     *
     * dealType은 기본값이 있어 비어 있을 수 없고, promotionalPhrase는 [Product]에서도
     * nullable이므로 둘 다 검사하지 않는다.
     */
    private fun resolve(command: PreparedProductDraftSaveCommand): DraftFailReason? =
        when {
            command.thumbnailUrl == null -> DraftFailReason.THUMBNAIL_MISSING
            command.category == null -> DraftFailReason.CATEGORY_MISSING
            command.name == null -> DraftFailReason.NAME_MISSING
            command.price == null -> DraftFailReason.PRICE_MISSING
            command.discountPeriod == null -> DraftFailReason.DISCOUNT_PERIOD_MISSING
            else -> null
        }
}
