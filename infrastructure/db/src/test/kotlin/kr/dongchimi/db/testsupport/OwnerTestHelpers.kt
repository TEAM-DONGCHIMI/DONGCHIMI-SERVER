package kr.dongchimi.db.testsupport

import java.sql.Connection

fun Connection.insertOwner(
    email: String,
    password: String = "password",
): Long {
    prepareStatement(
        "INSERT INTO owners (email, password) VALUES (?, ?) RETURNING owner_id",
    ).use { statement ->
        statement.setString(1, email)
        statement.setString(2, password)
        statement.executeQuery().use { resultSet ->
            resultSet.next()
            return resultSet.getLong("owner_id")
        }
    }
}

fun Connection.softDeleteOwner(id: Long) {
    prepareStatement("UPDATE owners SET deleted_at = now() WHERE owner_id = ?").use { statement ->
        statement.setLong(1, id)
        statement.executeUpdate()
    }
}
