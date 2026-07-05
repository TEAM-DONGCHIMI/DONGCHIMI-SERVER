package kr.dongchimi.core.admin

interface DefaultProductThumbnailRepository {
    fun findById(id: Long): DefaultProductThumbnail?

    fun save(defaultProductThumbnail: DefaultProductThumbnail): DefaultProductThumbnail
}
