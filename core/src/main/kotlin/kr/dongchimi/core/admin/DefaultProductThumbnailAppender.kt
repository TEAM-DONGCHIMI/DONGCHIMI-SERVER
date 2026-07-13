package kr.dongchimi.core.admin

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DefaultProductThumbnailAppender(
    private val defaultProductThumbnailRepository: DefaultProductThumbnailRepository,
) {
    @Transactional
    fun appendAll(
        command: DefaultThumbnailCreateCommand,
        createdBy: Long,
    ): List<DefaultProductThumbnail> {
        val thumbnails =
            command.items.map { item ->
                DefaultProductThumbnail(
                    name = item.name,
                    thumbnailUrl = item.thumbnailUrl,
                    category = item.category,
                    createdBy = createdBy,
                )
            }
        return defaultProductThumbnailRepository.saveAll(thumbnails)
    }
}
