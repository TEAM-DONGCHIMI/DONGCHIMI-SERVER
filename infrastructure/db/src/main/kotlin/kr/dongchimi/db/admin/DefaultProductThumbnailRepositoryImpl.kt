package kr.dongchimi.db.admin

import kr.dongchimi.core.admin.DefaultProductThumbnail
import kr.dongchimi.core.admin.DefaultProductThumbnailErrorCode
import kr.dongchimi.core.admin.DefaultProductThumbnailRepository
import kr.dongchimi.core.common.exception.CoreException
import kr.dongchimi.core.product.ProductCategory
import org.hibernate.exception.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class DefaultProductThumbnailRepositoryImpl(
    private val defaultProductThumbnailJpaRepository: DefaultProductThumbnailJpaRepository,
) : DefaultProductThumbnailRepository {
    override fun findById(id: Long): DefaultProductThumbnail? = defaultProductThumbnailJpaRepository.findByIdOrNull(id)?.toDomain()

    override fun findAllByCategoryIn(categories: Set<ProductCategory>): List<DefaultProductThumbnail> =
        defaultProductThumbnailJpaRepository.findAllByCategoryIn(categories).map { it.toDomain() }

    override fun findAllByLatest(
        search: String?,
        cursor: Long?,
        limit: Int,
    ): List<DefaultProductThumbnail> =
        defaultProductThumbnailJpaRepository
            .findAllByLatest(cursor, search, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun findAllByName(
        search: String?,
        cursor: Long?,
        cursorName: String?,
        limit: Int,
    ): List<DefaultProductThumbnail> =
        defaultProductThumbnailJpaRepository
            .findAllByName(cursor, cursorName, search, PageRequest.of(0, limit))
            .map { it.toDomain() }

    override fun findNameById(id: Long): String? = defaultProductThumbnailJpaRepository.findNameById(id)

    override fun save(defaultProductThumbnail: DefaultProductThumbnail): DefaultProductThumbnail =
        try {
            // 신규 생성은 IDENTITY 전략상 persist() 시점에 INSERT가 즉시 실행되지만, 기존 엔티티는
            // save()가 merge()를 타 UPDATE가 트랜잭션 커밋 시점까지 지연된다. 그러면 유니크 제약
            // 위반이 이 try-catch 밖에서 터져 THUMBNAIL_NAME_EXISTS로 변환되지 못하므로, flush
            // 시점을 여기로 강제로 맞춘다.
            defaultProductThumbnailJpaRepository.saveAndFlush(DefaultProductThumbnailJpaEntity(defaultProductThumbnail)).toDomain()
        } catch (exception: DataIntegrityViolationException) {
            throw exception.toThumbnailNameExistsOrSelf()
        }

    override fun saveAll(defaultProductThumbnails: List<DefaultProductThumbnail>): List<DefaultProductThumbnail> =
        try {
            defaultProductThumbnailJpaRepository
                .saveAll(defaultProductThumbnails.map { DefaultProductThumbnailJpaEntity(it) })
                .map { it.toDomain() }
        } catch (exception: DataIntegrityViolationException) {
            throw exception.toThumbnailNameExistsOrSelf()
        }

    private fun DataIntegrityViolationException.toThumbnailNameExistsOrSelf(): Throwable =
        if (isNameUniqueViolation()) CoreException(DefaultProductThumbnailErrorCode.THUMBNAIL_NAME_EXISTS) else this

    private fun DataIntegrityViolationException.isNameUniqueViolation(): Boolean =
        (cause as? ConstraintViolationException)?.constraintName == THUMBNAIL_NAME_UNIQUE_INDEX

    companion object {
        private const val THUMBNAIL_NAME_UNIQUE_INDEX = "uq_default_product_thumbnails_name"
    }
}
