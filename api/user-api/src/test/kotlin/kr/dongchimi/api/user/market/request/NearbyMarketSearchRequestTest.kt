package kr.dongchimi.api.user.market.request

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.core.common.exception.InvalidInputException

class NearbyMarketSearchRequestTest :
    FunSpec({
        test("radius와 size를 생략하면 기본값 1000m, 5개를 사용한다") {
            val condition = NearbyMarketSearchRequest(lat = 37.55, lng = 126.98).toSearchCondition()

            condition.origin.latitude shouldBe 37.55
            condition.origin.longitude shouldBe 126.98
            condition.radiusMeters shouldBe 1000.0
            condition.size shouldBe 5
            condition.cursorMarketId shouldBe null
        }

        test("위도가 없으면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                NearbyMarketSearchRequest(lng = 126.98).toSearchCondition()
            }.message shouldBe "위도는 필수로 입력해 주세요."
        }

        test("경도가 없으면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                NearbyMarketSearchRequest(lat = 37.55).toSearchCondition()
            }.message shouldBe "경도는 필수로 입력해 주세요."
        }

        test("위도가 유효 범위를 벗어나면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                NearbyMarketSearchRequest(lat = 90.1, lng = 126.98).toSearchCondition()
            }.message shouldBe "경도, 위도가 유효 범위를 벗어났습니다."
        }

        test("경도가 유효 범위를 벗어나면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                NearbyMarketSearchRequest(lat = 37.55, lng = -180.1).toSearchCondition()
            }.message shouldBe "경도, 위도가 유효 범위를 벗어났습니다."
        }

        test("반경이 0 이하면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                NearbyMarketSearchRequest(lat = 37.55, lng = 126.98, radius = 0.0).toSearchCondition()
            }.message shouldBe "반경은 0보다 커야 합니다."
        }

        test("조회 개수가 0 이하면 예외를 던진다") {
            shouldThrow<InvalidInputException> {
                NearbyMarketSearchRequest(lat = 37.55, lng = 126.98, size = 0).toSearchCondition()
            }.message shouldBe "조회 개수는 1 이상이어야 합니다."
        }

        test("경계값인 위도 90, 경도 -180은 허용한다") {
            val condition = NearbyMarketSearchRequest(lat = 90.0, lng = -180.0).toSearchCondition()

            condition.origin.latitude shouldBe 90.0
            condition.origin.longitude shouldBe -180.0
        }
    })
