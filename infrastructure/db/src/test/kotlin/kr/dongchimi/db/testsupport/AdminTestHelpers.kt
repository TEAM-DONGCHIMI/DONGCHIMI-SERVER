package kr.dongchimi.db.testsupport

import java.sql.Connection

fun Connection.insertAdmin(
    email: String,
    name: String = "admin",
    password: String = "password",
): Long {
    prepareStatement(
        "INSERT INTO admins (name, email, password) VALUES (?, ?, ?) RETURNING admin_id",
    ).use { statement ->
        statement.setString(1, name)
        statement.setString(2, email)
        statement.setString(3, password)
        statement.executeQuery().use { resultSet ->
            resultSet.next()
            return resultSet.getLong("admin_id")
        }
    }
}
