package kr.dongchimi.infrastructure.redis

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kr.dongchimi.core.product.ImportJobProgress
import kr.dongchimi.core.product.ImportStep
import kr.dongchimi.core.product.ImportStepProgress
import kr.dongchimi.core.product.ImportStepStatus
import kr.dongchimi.infrastructure.redis.testsupport.TestRedisContainer
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import tools.jackson.module.kotlin.jacksonObjectMapper
import java.util.UUID

class RedisImportJobProgressStoreTest :
    FunSpec({
        val connectionFactory =
            LettuceConnectionFactory(RedisStandaloneConfiguration(TestRedisContainer.host(), TestRedisContainer.port()))
        connectionFactory.afterPropertiesSet()
        val store = RedisImportJobProgressStore(StringRedisTemplate(connectionFactory), jacksonObjectMapper())

        test("save한 스냅샷을 find로 그대로 조회한다") {
            val jobId = "imp_${UUID.randomUUID()}"
            val progress =
                ImportJobProgress(
                    jobId = jobId,
                    progress = 72,
                    remainingSeconds = 10,
                    currentStep = ImportStep.NAME_EXTRACTION,
                    steps =
                        listOf(
                            ImportStepProgress(ImportStep.FILE_UPLOAD, ImportStepStatus.COMPLETED),
                            ImportStepProgress(ImportStep.NAME_EXTRACTION, ImportStepStatus.IN_PROGRESS),
                            ImportStepProgress(ImportStep.PRICE_EXTRACTION, ImportStepStatus.PENDING),
                            ImportStepProgress(ImportStep.CATEGORY_CLASSIFICATION, ImportStepStatus.PENDING),
                            ImportStepProgress(ImportStep.IMAGE_MATCHING, ImportStepStatus.PENDING),
                        ),
                )

            store.save(progress)

            store.find(jobId) shouldBe progress
        }

        test("저장된 적 없는 jobId를 조회하면 null이다") {
            store.find("imp_${UUID.randomUUID()}").shouldBeNull()
        }

        test("remainingSeconds/currentStep이 null인 스냅샷도 왕복한다 (claim 직후, 첫 진행률 나오기 전)") {
            val jobId = "imp_${UUID.randomUUID()}"
            val progress =
                ImportJobProgress(
                    jobId = jobId,
                    progress = 0,
                    remainingSeconds = null,
                    currentStep = null,
                    steps = ImportStep.entries.map { ImportStepProgress(it, ImportStepStatus.PENDING) },
                )

            store.save(progress)

            store.find(jobId) shouldBe progress
        }
    })
