package kr.dongchimi.api.owner.auth

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.auth.request.OwnerSignupRequest
import kr.dongchimi.api.owner.auth.response.OwnerSignupResponse
import kr.dongchimi.core.owner.OwnerSignupService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/auth/signup")
class OwnerSignupController(
    private val ownerSignupService: OwnerSignupService,
) : OwnerSignupApi {
    @PostMapping
    override fun signup(
        @RequestBody request: OwnerSignupRequest,
    ): ApiResponse<OwnerSignupResponse> {
        val owner = ownerSignupService.signup(request.toCommand())
        return ApiResponse.success(OwnerSignupResponse(ownerId = owner.id, email = owner.email))
    }
}
