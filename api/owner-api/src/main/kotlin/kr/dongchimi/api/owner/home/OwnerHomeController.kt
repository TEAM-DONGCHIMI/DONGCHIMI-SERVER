package kr.dongchimi.api.owner.home

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.home.response.OwnerHomeResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/home")
class OwnerHomeController(
    private val ownerHomeQueryFacade: OwnerHomeQueryFacade,
) : OwnerHomeApi {
    @GetMapping
    override fun getHome(owner: OwnerApiUser): ApiResponse<OwnerHomeResponse> =
        ApiResponse.success(ownerHomeQueryFacade.getHome(owner.userId))
}
