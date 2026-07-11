package kr.dongchimi.core.viewcount

interface ViewCountStore {
    /** 유저 단위 중복을 제거하며 조회를 1회 누적한다(윈도우 내 같은 유저 재조회는 무시). */
    fun record(
        target: ViewTarget,
        targetId: Long,
        userId: Long,
    )

    /** 누적분을 원자적으로 걷어내 대상 id별 증가량으로 반환한다. 걷어낸 뒤 저장소에서 비운다. */
    fun drain(target: ViewTarget): Map<Long, Int>
}
