// [임시] 엑셀 분석 파이프라인 동기/비동기 성능 비교용. 벤치마크 종료 후 제거.
package kr.dongchimi.api.owner.product

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.ProductImportRequest
import kr.dongchimi.api.owner.product.response.ProductImportSyncResponse
import kr.dongchimi.core.product.importjob.sync.SyncImportJobService
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/owners/markets/{marketId}/products/import/sync")
class OwnerProductImportSyncController(
    private val syncImportJobService: SyncImportJobService,
) : OwnerProductImportSyncApi {
    @PostMapping
    override fun importSync(
        apiUser: OwnerApiUser,
        @PathVariable marketId: Long,
        @RequestBody request: ProductImportRequest,
    ): ApiResponse<ProductImportSyncResponse> {
        val command = request.toCommand()
        val result = syncImportJobService.analyze(apiUser.userId, marketId, command.excelFileUrl)

        return ApiResponse.success(ProductImportSyncResponse(result))
    }
}
