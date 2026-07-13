// [임시] 엑셀 분석 파이프라인 동기/비동기 성능 비교용. 벤치마크 종료 후 제거.
package kr.dongchimi.core.product.importjob.sync

/**
 * 동기 분석 1회의 결과. 카운트와 단계별 소요시간(ms)을 함께 담아 어느 단계가 병목인지 바로 본다.
 */
data class SyncImportResult(
    val totalCount: Int,
    val successCount: Int,
    val failCount: Int,
    val elapsedMs: Long,
    val stageElapsedMs: StageElapsedMs,
)

/** download~persist 각 단계의 소요시간(ms). classify/match가 대부분을 차지할 것으로 예상된다. */
data class StageElapsedMs(
    val download: Long,
    val parse: Long,
    val classify: Long,
    val match: Long,
    val persist: Long,
)
