package kr.dongchimi.db.admin

import kr.dongchimi.core.admin.DefaultProductThumbnail
import kr.dongchimi.core.admin.DefaultProductThumbnailRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class DefaultProductThumbnailRepositoryImpl(
    private val defaultProductThumbnailJpaRepository: DefaultProductThumbnailJpaRepository,
) : DefaultProductThumbnailRepository {
    override fun findById(id: Long): DefaultProductThumbnail? = defaultProductThumbnailJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun save(defaultProductThumbnail: DefaultProductThumbnail): DefaultProductThumbnail =
        defaultProductThumbnailJpaRepository.save(DefaultProductThumbnailJpaEntity(defaultProductThumbnail)).toDomain()
}
