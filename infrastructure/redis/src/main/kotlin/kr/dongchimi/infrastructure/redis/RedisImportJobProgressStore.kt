package kr.dongchimi.infrastructure.redis

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.dongchimi.core.product.ImportJobProgress
import kr.dongchimi.core.product.ImportJobProgressStore
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import tools.jackson.databind.ObjectMapper
import java.time.Duration

/**
 * TTL은 죽은 워커 판정용이 아니라 키 누수 방지용이다(계획서 §3-3) — 죽은 워커 판정은
 * DB 리스가 담당하므로 heartbeat로 갱신할 필요가 없다.
 */
@Component
class RedisImportJobProgressStore(
    private val stringRedisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
) : ImportJobProgressStore {
    override suspend fun save(progress: ImportJobProgress) {
        withContext(Dispatchers.IO) {
            stringRedisTemplate
                .opsForValue()
                .set(ImportJobRedisKeys.snapshot(progress.jobId), objectMapper.writeValueAsString(progress), SNAPSHOT_TTL)
        }
    }

    override suspend fun find(jobId: String): ImportJobProgress? =
        withContext(Dispatchers.IO) {
            stringRedisTemplate
                .opsForValue()
                .get(ImportJobRedisKeys.snapshot(jobId))
                ?.let { objectMapper.readValue(it, ImportJobProgress::class.java) }
        }

    companion object {
        private val SNAPSHOT_TTL = Duration.ofHours(1)
    }
}
