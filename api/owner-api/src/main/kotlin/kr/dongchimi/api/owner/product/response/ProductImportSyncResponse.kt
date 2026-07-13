// [임시] 엑셀 분석 파이프라인 동기/비동기 성능 비교용. 벤치마크 종료 후 제거.
package kr.dongchimi.api.owner.product.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.dongchimi.core.product.importjob.sync.StageElapsedMs
import kr.dongchimi.core.product.importjob.sync.SyncImportResult

data class ProductImportSyncResponse(
    @Schema(description = "전체 분석 상품 수")
    val totalCount: Int,
    @Schema(description = "분석 성공(등록완료) 상품 수")
    val successCount: Int,
    @Schema(description = "분석 실패(수정필요) 상품 수")
    val failCount: Int,
    @Schema(description = "download~persist 전체 소요시간(ms)")
    val elapsedMs: Long,
    @Schema(description = "단계별 소요시간(ms)")
    val stageElapsedMs: StageElapsedMs,
) {
    constructor(result: SyncImportResult) : this(
        totalCount = result.totalCount,
        successCount = result.successCount,
        failCount = result.failCount,
        elapsedMs = result.elapsedMs,
        stageElapsedMs = result.stageElapsedMs,
    )
}
