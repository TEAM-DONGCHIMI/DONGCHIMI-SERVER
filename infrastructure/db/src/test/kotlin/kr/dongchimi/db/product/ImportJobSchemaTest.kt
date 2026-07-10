package kr.dongchimi.db.product

import io.kotest.assertions.throwables.shouldThrow
import kr.dongchimi.db.testsupport.ConstraintSpec
import kr.dongchimi.db.testsupport.insertImportJob
import org.postgresql.util.PSQLException

class ImportJobSchemaTest :
    ConstraintSpec(tableName = "product_import_jobs", body = {
        test("서로 다른 job_id로 여러 작업을 저장할 수 있다") {
            connection.insertImportJob(jobId = "imp_test1")
            connection.insertImportJob(jobId = "imp_test2")
        }

        test("같은 job_id로 두 번 저장하면 실패한다") {
            connection.insertImportJob(jobId = "imp_test1")

            shouldThrow<PSQLException> {
                connection.insertImportJob(jobId = "imp_test1")
            }
        }
    })
