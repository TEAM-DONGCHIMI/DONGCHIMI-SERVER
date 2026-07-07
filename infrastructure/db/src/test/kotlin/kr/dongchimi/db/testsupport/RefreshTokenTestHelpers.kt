package kr.dongchimi.db.testsupport

import java.sql.Connection
import java.sql.Timestamp
import java.time.LocalDateTime

fun Connection.insertRefreshToken(
    tokenId: String,
    userId: Long,
    expiresAt: LocalDateTime = LocalDateTime.now().plusDays(14),
): String {
    prepareStatement(
        "INSERT INTO refresh_tokens (token_id, user_id, expires_at) VALUES (?, ?, ?) RETURNING token_id",
    ).use { statement ->
        statement.setString(1, tokenId)
        statement.setLong(2, userId)
        statement.setTimestamp(3, Timestamp.valueOf(expiresAt))
        statement.executeQuery().use { resultSet ->
            resultSet.next()
            return resultSet.getString("token_id")
        }
    }
}
