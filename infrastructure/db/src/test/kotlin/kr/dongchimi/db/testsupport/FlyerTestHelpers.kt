package kr.dongchimi.db.testsupport

import java.sql.Connection

fun Connection.insertFlyer(
    marketId: Long,
    slug: String,
) {
    prepareStatement(
        "INSERT INTO flyers (market_id, slug) VALUES (?, ?)",
    ).use { statement ->
        statement.setLong(1, marketId)
        statement.setString(2, slug)
        statement.executeUpdate()
    }
}
