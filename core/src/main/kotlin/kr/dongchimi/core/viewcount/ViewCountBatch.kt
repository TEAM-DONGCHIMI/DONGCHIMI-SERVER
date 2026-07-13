package kr.dongchimi.core.viewcount

/**
 * drain으로 걷어낸 조회수 누적분 한 묶음. [token]은 저장소가 이 배치를 격리 보관한
 * 위치를 식별하며, commit·restore 시 해당 배치를 정확히 지목하는 데 쓰인다.
 */
data class ViewCountBatch(
    val target: ViewTarget,
    val token: String,
    val deltas: Map<Long, Int>,
)
