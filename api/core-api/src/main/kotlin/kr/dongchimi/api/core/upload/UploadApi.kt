package kr.dongchimi.api.core.upload

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.common.swagger.ApiErrorCodes
import kr.dongchimi.api.core.upload.request.PresignedUploadRequest
import kr.dongchimi.api.core.upload.request.PresignedUploadsRequest
import kr.dongchimi.api.core.upload.response.PresignedUploadResponse
import kr.dongchimi.api.core.upload.response.PresignedUploadsResponse
import kr.dongchimi.core.common.exception.CommonErrorCode
import kr.dongchimi.core.upload.UploadErrorCode

@Tag(name = "Upload", description = "Presigned URL 기반 S3 업로드 API")
interface UploadApi {
    @Operation(
        summary = "업로드 URL 발급",
        description =
            "S3에 직접 업로드할 수 있는 짧은 수명의 Presigned PUT URL을 발급한다. " +
                "발급된 objectKey는 tmp 경로이며, 실제 리소스 생성 요청 시 도메인 폴더로 이동된다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, UploadErrorCode::class)
    fun createPresignedUrl(request: PresignedUploadRequest): ApiResponse<PresignedUploadResponse>

    @Operation(
        summary = "업로드 URL 일괄 발급",
        description =
            "여러 건의 업로드 URL을 한 번의 요청으로 발급한다. 항목별 정책은 단건 발급과 동일하며, " +
                "목록 중 하나라도 검증에 실패하면 전체 요청이 실패한다.",
    )
    @ApiErrorCodes(CommonErrorCode::class, UploadErrorCode::class)
    fun createPresignedUrls(request: PresignedUploadsRequest): ApiResponse<PresignedUploadsResponse>
}
