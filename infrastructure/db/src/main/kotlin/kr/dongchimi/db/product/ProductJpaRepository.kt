package kr.dongchimi.db.product

import org.springframework.data.jpa.repository.JpaRepository

interface ProductJpaRepository : JpaRepository<ProductJpaEntity, Long>
