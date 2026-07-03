package kr.dongchimi.core.market

interface FlyerRepository {
    fun findById(id: Long): Flyer?

    fun save(flyer: Flyer): Flyer
}
