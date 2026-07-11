package kr.dongchimi.core.viewcount

/** 상세 조회 성공 시 QueryFacade가 발행하는 이벤트. [ViewCountEventListener]가 커밋 후 처리한다. */
data class EntityViewedEvent(
    val target: ViewTarget,
    val targetId: Long,
    val userId: Long,
)
