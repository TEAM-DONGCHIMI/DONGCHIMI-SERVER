// [임시] 엑셀 분석 파이프라인 동기/비동기 성능 비교용. 벤치마크 종료 후 제거.
package kr.dongchimi.core.product.importjob.sync

import kr.dongchimi.core.product.PreparedProduct
import kr.dongchimi.core.product.PreparedProductRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 마트의 기존 draft를 soft delete하고 분석 결과를 새로 insert한다. 두 작업은 원자 단위라 Implement
 * 레이어에 트랜잭션 경계를 둔다(기존 [kr.dongchimi.core.product.importjob.ImportJobFinisher.complete]와
 * 같은 판단). job CAS에 얽히지 않도록 persist만 하는 얇은 컴포넌트로 둔다.
 */
@Component
class SyncImportPersister(
    private val preparedProductRepository: PreparedProductRepository,
) {
    @Transactional
    fun persist(
        marketId: Long,
        drafts: List<PreparedProduct>,
    ) {
        preparedProductRepository.softDeleteAllByMarketId(marketId)
        preparedProductRepository.saveAll(drafts)
    }
}
