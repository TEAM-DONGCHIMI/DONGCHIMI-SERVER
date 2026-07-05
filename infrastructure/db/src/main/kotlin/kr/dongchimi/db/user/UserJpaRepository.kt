package kr.dongchimi.db.user

import org.springframework.data.jpa.repository.JpaRepository

interface UserJpaRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): UserJpaEntity?
}
