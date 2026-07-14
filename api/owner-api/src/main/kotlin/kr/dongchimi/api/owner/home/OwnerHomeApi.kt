package kr.dongchimi.api.owner.home

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.home.response.OwnerHomeResponse

@Tag(name = "Owner Home", description = "점주 홈 화면 조회 API")
interface OwnerHomeApi {
    @Operation(
        summary = "점주 홈 화면 조회",
        description = "오늘 등록한 상품 수, 진행 중인 오늘의 특가·기간 할인 상품 현황을 조회한다. 마트를 아직 등록하지 않았다면 모든 값이 0/빈 배열로 내려온다.",
    )
    fun getHome(
        @Parameter(hidden = true) owner: OwnerApiUser,
    ): ApiResponse<OwnerHomeResponse>
}
