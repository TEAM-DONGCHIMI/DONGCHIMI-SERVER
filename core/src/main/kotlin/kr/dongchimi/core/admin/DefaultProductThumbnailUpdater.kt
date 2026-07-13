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
        val updatedRows =
            defaultProductThumbnailRepository.updateContent(
                id = command.id,
                name = command.name,
                thumbnailUrl = command.thumbnailUrl,
                category = command.category,
            )

        if (updatedRows == 0) {
            throw CoreException(DefaultProductThumbnailErrorCode.THUMBNAIL_NOT_FOUND)
        }
    }
}
