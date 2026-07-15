package kr.dongchimi.db.owner

import org.springframework.data.jpa.repository.JpaRepository

interface OwnerJpaRepository : JpaRepository<OwnerJpaEntity, Long> {
    fun findByIdAndDeletedAtIsNull(id: Long): OwnerJpaEntity?

    fun findByEmailAndDeletedAtIsNull(email: String): OwnerJpaEntity?

    fun existsByEmailAndDeletedAtIsNull(email: String): Boolean

    fun existsByIdAndDeletedAtIsNull(id: Long): Boolean
}
