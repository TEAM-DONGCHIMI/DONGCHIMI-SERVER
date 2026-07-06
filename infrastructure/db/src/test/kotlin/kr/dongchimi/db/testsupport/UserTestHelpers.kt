package kr.dongchimi.db.testsupport

import java.sql.Connection

fun Connection.insertUser(
    email: String,
    socialProvider: String = "KAKAO",
    socialId: String = email,
    gender: String = "M",
): Long {
    prepareStatement(
        "INSERT INTO users (email, social_provider, social_id, gender) VALUES (?, ?, ?, ?) RETURNING user_id",
    ).use { statement ->
        statement.setString(1, email)
        statement.setString(2, socialProvider)
        statement.setString(3, socialId)
        statement.setString(4, gender)
        statement.executeQuery().use { resultSet ->
            resultSet.next()
            return resultSet.getLong("user_id")
        }
    }
}

fun Connection.softDeleteUser(id: Long) {
    prepareStatement("UPDATE users SET deleted_at = now() WHERE user_id = ?").use { statement ->
        statement.setLong(1, id)
        statement.executeUpdate()
    }
}
