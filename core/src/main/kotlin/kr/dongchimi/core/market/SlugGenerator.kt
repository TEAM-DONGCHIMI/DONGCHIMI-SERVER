package kr.dongchimi.core.market

import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.Base64
import java.util.UUID

@Component
class SlugGenerator {
    fun generate(): String {
        val uuid = UUID.randomUUID()
        val bytes =
            ByteBuffer
                .allocate(16)
                .putLong(uuid.mostSignificantBits)
                .putLong(uuid.leastSignificantBits)
                .array()
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}
