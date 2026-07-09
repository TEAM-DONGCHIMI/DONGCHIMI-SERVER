package kr.dongchimi.db.testsupport

import java.sql.Connection

fun Connection.insertImportJob(
    jobId: String,
    marketId: Long = 1L,
    ownerId: Long = 1L,
    excelObjectKey: String = "tmp/import/test.xlsx",
    status: String = "PENDING",
): String {
    prepareStatement(
        """
        INSERT INTO product_import_jobs (job_id, market_id, owner_id, excel_object_key, status)
        VALUES (?, ?, ?, ?, ?)
        RETURNING job_id
        """,
    ).use { statement ->
        statement.setString(1, jobId)
        statement.setLong(2, marketId)
        statement.setLong(3, ownerId)
        statement.setString(4, excelObjectKey)
        statement.setString(5, status)
        statement.executeQuery().use { resultSet ->
            resultSet.next()
            return resultSet.getString("job_id")
        }
    }
}
