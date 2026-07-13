package kr.dongchimi.core.admin

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DefaultProductThumbnailUpdater(
    private val defaultProductThumbnailRepository: DefaultProductThumbnailRepository,
) {
    @Transactional
    fun update(command: DefaultThumbnailUpdateCommand) {
        val thumbnail =
            defaultProductThumbnailRepository.findById(command.id)
                ?: throw CoreException(DefaultProductThumbnailErrorCode.THUMBNAIL_NOT_FOUND)

        val updated =
            thumbnail.copy(
                name = command.name,
                thumbnailUrl = command.thumbnailUrl,
                category = command.category,
            )

        defaultProductThumbnailRepository.save(updated)
    }
}
