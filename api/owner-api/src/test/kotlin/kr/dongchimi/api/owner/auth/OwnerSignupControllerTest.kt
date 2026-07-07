package kr.dongchimi.api.owner.auth

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.dongchimi.api.owner.auth.request.OwnerSignupRequest
import kr.dongchimi.core.owner.Owner
import kr.dongchimi.core.owner.OwnerSignupCommand
import kr.dongchimi.core.owner.OwnerSignupService
import org.mockito.Mockito

class OwnerSignupControllerTest :
    FunSpec({
        test("회원가입 성공 시 ownerId와 email을 반환한다") {
            val ownerSignupService = Mockito.mock(OwnerSignupService::class.java)
            Mockito
                .`when`(ownerSignupService.signup(OwnerSignupCommand("owner@dongchimi.kr", "password123!")))
                .thenReturn(Owner(id = 1L, email = "owner@dongchimi.kr", password = "encoded"))
            val controller = OwnerSignupController(ownerSignupService)

            val result = controller.signup(OwnerSignupRequest("owner@dongchimi.kr", "password123!"))

            result.data?.ownerId shouldBe 1L
            result.data?.email shouldBe "owner@dongchimi.kr"
        }
    })
