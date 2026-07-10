package kr.dongchimi.api.owner.product

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.ProductImportRequest
import kr.dongchimi.core.product.import.ImportJob
import kr.dongchimi.core.product.import.ImportJobEvent
import kr.dongchimi.core.product.import.ImportJobService
import kr.dongchimi.core.product.import.ImportJobStatus
import org.mockito.Mockito

class OwnerProductImportControllerTest :
    FunSpec({
        val apiUser = OwnerApiUser(userId = 1L, roles = setOf("OWNER"))
        val marketId = 10L

        fun newController(importJobService: ImportJobService = Mockito.mock(ImportJobService::class.java)): OwnerProductImportController =
            OwnerProductImportController(importJobService)

        test("분석 시작 시 검증된 excelFileUrl로 서비스를 호출한다") {
            val importJobService = Mockito.mock(ImportJobService::class.java)
            val job = sampleJob()
            Mockito
                .`when`(importJobService.startImport(1L, marketId, "https://cdn.example.com/x.xlsx"))
                .thenReturn(job)
            val controller = newController(importJobService)
            val request = ProductImportRequest(excelFileUrl = "https://cdn.example.com/x.xlsx")

            val response = controller.startImport(apiUser, marketId, request)

            response.success shouldBe true
            response.data!!.jobId shouldBe "imp_test"
        }

        test("진행상태 구독 시 userId·marketId·jobId로 서비스를 호출하고 SseEmitter를 반환한다") {
            val importJobService = Mockito.mock(ImportJobService::class.java)
            Mockito
                .`when`(importJobService.subscribeProgress(1L, marketId, "imp_test"))
                .thenReturn(flowOf(ImportJobEvent.Canceled("imp_test")))
            val controller = newController(importJobService)

            val emitter = controller.subscribeProgress(apiUser, marketId, "imp_test")

            emitter.shouldNotBeNull()
            Mockito.verify(importJobService).subscribeProgress(1L, marketId, "imp_test")
        }

        test("취소 시 userId·marketId·jobId로 서비스를 호출한다") {
            val importJobService = Mockito.mock(ImportJobService::class.java)
            val controller = newController(importJobService)

            val response = controller.cancelImport(apiUser, marketId, "imp_test")

            response.success shouldBe true
            Mockito.verify(importJobService).cancel(1L, marketId, "imp_test")
        }
    })

private fun sampleJob(): ImportJob =
    ImportJob(
        jobId = "imp_test",
        marketId = 10L,
        ownerId = 1L,
        excelObjectKey = "products/imports/test.xlsx",
        status = ImportJobStatus.PENDING,
    )
