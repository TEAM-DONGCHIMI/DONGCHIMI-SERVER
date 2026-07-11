package kr.dongchimi.core.product.importjob

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "import.job")
data class ImportJobProperties(
    /** 큐 claim 시 거는 리스 기간. 갱신 주기는 이 값의 1/3이다. */
    val lease: Duration,
    /** 인스턴스가 새 작업을 집으러 가는 주기. */
    val pollInterval: Duration,
    /** 인스턴스당 동시에 처리할 수 있는 작업 수. */
    val slotsPerInstance: Int,
    /** claim 상한 — 이 값 이상 시도된 작업은 더 이상 집지 않고 FAILED로 남는다. */
    val maxAttempts: Int,
    /** 카테고리 분류·이미지 매칭 단계에서 동시에 띄우는 배치(청크) 호출 상한. */
    val aiConcurrency: Int,
    /** 배치 호출 하나에 담는 항목 수 상한. */
    val aiBatchSize: Int,
)
