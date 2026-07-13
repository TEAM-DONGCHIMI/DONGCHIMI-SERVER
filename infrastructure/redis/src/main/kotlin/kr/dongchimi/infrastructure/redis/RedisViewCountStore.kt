package kr.dongchimi.infrastructure.redis

import kr.dongchimi.core.viewcount.ViewCountBatch
import kr.dongchimi.core.viewcount.ViewCountProperties
import kr.dongchimi.core.viewcount.ViewCountStore
import kr.dongchimi.core.viewcount.ViewTarget
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RedisViewCountStore(
    private val stringRedisTemplate: StringRedisTemplate,
    private val properties: ViewCountProperties,
) : ViewCountStore {
    override fun record(
        target: ViewTarget,
        targetId: Long,
        userId: Long,
    ) {
        // SET NX가 원자적이라 같은 유저의 동시 요청 중 하나만 true → 정확히 1회만 증가한다.
        val firstView =
            stringRedisTemplate
                .opsForValue()
                .setIfAbsent(ViewCountRedisKeys.dedup(target, targetId, userId), "1", properties.dedupTtl) ?: false

        if (firstView) {
            stringRedisTemplate
                .opsForHash<String, String>()
                .increment(ViewCountRedisKeys.pending(target), targetId.toString(), 1)
        }
    }

    override fun drain(target: ViewTarget): ViewCountBatch? {
        val pendingKey = ViewCountRedisKeys.pending(target)
        if (stringRedisTemplate.hasKey(pendingKey) != true) return null

        // pending을 고유 키로 원자적으로 옮겨 이 인스턴스만 그 배치를 소유하게 한다.
        // 여러 인스턴스가 동시에 걷어도 RENAME이 직렬화되어 한쪽만 성공, 나머지는 소스 없음으로 null.
        val token = UUID.randomUUID().toString()
        val flushKey = ViewCountRedisKeys.flush(target, token)
        try {
            stringRedisTemplate.rename(pendingKey, flushKey)
        } catch (e: Exception) {
            return null
        }

        // DB 반영이 끝날 때까지 flush 키를 지우지 않고 격리 보관한다. commit/restore로 마무리한다.
        val entries = stringRedisTemplate.opsForHash<String, String>().entries(flushKey)
        if (entries.isEmpty()) {
            stringRedisTemplate.delete(flushKey)
            return null
        }

        val deltas = entries.entries.associate { it.key.toLong() to it.value.toInt() }
        return ViewCountBatch(target, token, deltas)
    }

    override fun commit(batch: ViewCountBatch) {
        stringRedisTemplate.delete(ViewCountRedisKeys.flush(batch.target, batch.token))
    }

    override fun restore(batch: ViewCountBatch) {
        // DB 반영 실패분을 pending에 다시 더해 다음 flush에서 재처리한다. HINCRBY는 원자적·가산적이라
        // 그 사이 들어온 신규 조회수와 안전하게 합쳐진다. 되돌린 뒤 격리 보관분은 제거한다.
        val pendingKey = ViewCountRedisKeys.pending(batch.target)
        val hashOps = stringRedisTemplate.opsForHash<String, String>()
        batch.deltas.forEach { (targetId, delta) ->
            hashOps.increment(pendingKey, targetId.toString(), delta.toLong())
        }
        stringRedisTemplate.delete(ViewCountRedisKeys.flush(batch.target, batch.token))
    }
}
