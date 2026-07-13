// [임시] 엑셀 분석 파이프라인 동기/비동기 성능 비교용. 벤치마크 종료 후 제거.
package kr.dongchimi.api.owner.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.ProductImportRequest
import kr.dongchimi.api.owner.product.response.ProductImportSyncResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.product.importjob.ImportJobErrorCode
import org.springframework.web.bind.annotation.PathVariable

@Tag(name = "Product Import (Sync, 임시)", description = "[임시] 엑셀 분석 동기 파이프라인 — 성능 비교용")
interface OwnerProductImportSyncApi {
    @Operation(
        summary = "[임시] 상품 등록 엑셀 동기 분석",
        description =
            "엑셀 분석을 큐·SSE 없이 요청 스레드에서 블로킹으로 끝까지 수행하고 결과 카운트와 단계별 소요시간을 반환한다. " +
                "비동기 파이프라인과의 성능 비교 전용이며, 분석 결과는 기존과 동일하게 prepared_products에 반영된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ImportJobErrorCode::class)
    fun importSync(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: ProductImportRequest,
    ): ApiResponse<ProductImportSyncResponse>
}
