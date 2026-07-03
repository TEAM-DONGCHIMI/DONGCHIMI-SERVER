package kr.dongchimi.db.owner

import org.springframework.data.jpa.repository.JpaRepository

interface OwnerJpaRepository : JpaRepository<OwnerJpaEntity, Long>
