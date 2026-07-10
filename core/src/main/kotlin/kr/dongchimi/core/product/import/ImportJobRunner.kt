package kr.dongchimi.core.product.import

/**
 * [ImportJobPoller]가 claim한 job 하나를 넘겨 실행을 위임하는 경계. [ImportJobProcessor]가
 * 유일한 실제 구현이지만, Poller가 Processor의 전체 의존성 그래프(파서·AI·Redis 등)를 몰라도
 * 되게 분리해 Poller 테스트에서 가벼운 fake로 바꿔 끼울 수 있다.
 */
fun interface ImportJobRunner {
    suspend fun run(
        job: ImportJob,
        instanceId: String,
    )
}
