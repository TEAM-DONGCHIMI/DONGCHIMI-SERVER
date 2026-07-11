package kr.dongchimi.db.admin

import org.springframework.data.jpa.repository.JpaRepository

interface AdminJpaRepository : JpaRepository<AdminJpaEntity, Long> {
    fun findByEmail(email: String): AdminJpaEntity?

    fun existsByEmail(email: String): Boolean
}
