package kr.dongchimi.core.viewcount

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "view-count")
data class ViewCountProperties(
    /** 유저 단위 중복 제거 윈도우. 같은 유저의 같은 대상 재조회는 이 기간 내 1회만 집계된다. */
    val dedupTtl: Duration,
)
