package kr.dongchimi.db.market

import org.springframework.data.jpa.repository.JpaRepository

interface FlyerJpaRepository : JpaRepository<FlyerJpaEntity, Long>
