package kr.dongchimi.db.product

import org.springframework.data.jpa.repository.JpaRepository

interface ProductMetadataJpaRepository : JpaRepository<ProductMetadataJpaEntity, Long>
