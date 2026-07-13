package kr.dongchimi.core.viewcount

interface ViewCountStore {
    /** 유저 단위 중복을 제거하며 조회를 1회 누적한다(윈도우 내 같은 유저 재조회는 무시). */
    fun record(
        target: ViewTarget,
        targetId: Long,
        userId: Long,
    )

    /**
     * 누적분을 원자적으로 걷어내 배치로 반환한다(없으면 null). 걷어낸 배치는 저장소에
     * 격리 보관되며, DB 반영이 끝난 뒤 성공 시 [commit], 실패 시 [restore]로 마무리해야 한다.
     */
    fun drain(target: ViewTarget): ViewCountBatch?

    /** DB 반영에 성공한 배치를 저장소에서 최종 제거한다. */
    fun commit(batch: ViewCountBatch)

    /** DB 반영에 실패한 배치를 누적분으로 되돌려 다음 flush에서 재처리되게 한다. */
    fun restore(batch: ViewCountBatch)
}
