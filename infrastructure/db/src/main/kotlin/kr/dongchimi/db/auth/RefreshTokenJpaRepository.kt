package kr.dongchimi.db.auth

import org.springframework.data.jpa.repository.JpaRepository

interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, String> {
    fun deleteByTokenId(tokenId: String): Long
}
