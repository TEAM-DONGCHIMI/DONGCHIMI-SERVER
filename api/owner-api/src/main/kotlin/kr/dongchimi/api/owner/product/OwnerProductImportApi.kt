package kr.dongchimi.api.owner.product

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.owner.OwnerApiUser
import kr.dongchimi.api.owner.product.request.ProductImportRequest
import kr.dongchimi.api.owner.product.response.ProductImportResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.market.MarketErrorCode
import kr.dongchimi.core.product.import.ImportJobErrorCode
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "Product Import", description = "상품 등록 엑셀 분석 API")
interface OwnerProductImportApi {
    @Operation(
        summary = "상품 등록 엑셀파일에서 분석",
        description = "엑셀 파일 업로드 후, 파일 분석을 통해 상품 목록을 가져오기 위한 작업을 시작한다. 분석은 비동기로 진행되며 반환된 jobId로 진행상태를 구독한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ImportJobErrorCode::class)
    fun startImport(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        request: ProductImportRequest,
    ): ApiResponse<ProductImportResponse>

    @Operation(
        summary = "상품 등록 진행상태 조회 (SSE)",
        description =
            "업로드한 엑셀 파일의 AI 상품 분석 작업 진행상황을 SSE로 실시간 스트리밍한다. 구독 시 현재 상태를 즉시 1회 전송하고, " +
                "이후 상태가 바뀔 때마다 이벤트를 push한다. 완료(completed)·실패(failed)·취소(canceled) 이벤트 전송 후 스트림을 종료한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ImportJobErrorCode::class)
    fun subscribeProgress(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        @Parameter(description = "분석 작업 ID") @PathVariable jobId: String,
    ): SseEmitter

    @Operation(
        summary = "상품 등록 분석 취소",
        description = "AI가 상품 분석 중 취소한다. 응답은 취소 요청이 접수됐다는 뜻이고, 실제 취소 완료는 SSE의 canceled 이벤트로 알 수 있다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, MarketErrorCode::class, ImportJobErrorCode::class)
    suspend fun cancelImport(
        @Parameter(hidden = true) apiUser: OwnerApiUser,
        @Parameter(description = "마트 ID") @PathVariable marketId: Long,
        @Parameter(description = "분석 작업 ID") @PathVariable jobId: String,
    ): ApiResponse<Unit>
}
