package kr.dongchimi.bootstrap.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.util.TimeZone

@Configuration
class TimeZoneConfig {
    @PostConstruct
    fun setDefaultTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }
}
