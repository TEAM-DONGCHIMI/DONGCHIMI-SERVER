package kr.dongchimi.api.core.upload

import kr.dongchimi.api.core.common.dto.ApiResponse
import kr.dongchimi.api.core.upload.request.PresignedUploadRequest
import kr.dongchimi.api.core.upload.request.PresignedUploadsRequest
import kr.dongchimi.api.core.upload.response.PresignedUploadResponse
import kr.dongchimi.api.core.upload.response.PresignedUploadsResponse
import kr.dongchimi.core.upload.UploadService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/uploads")
class UploadController(
    private val uploadService: UploadService,
) : UploadApi {
    @PostMapping("/presigned-url")
    override fun createPresignedUrl(
        @RequestBody request: PresignedUploadRequest,
    ): ApiResponse<PresignedUploadResponse> {
        val presignedUpload = uploadService.createPresignedUpload(request.toCommand())

        return ApiResponse.success(
            PresignedUploadResponse(presignedUpload),
        )
    }

    @PostMapping("/presigned-urls")
    override fun createPresignedUrls(
        @RequestBody request: PresignedUploadsRequest,
    ): ApiResponse<PresignedUploadsResponse> {
        val presignedUploads = uploadService.createPresignedUploads(request.toCommands())

        return ApiResponse.success(
            PresignedUploadsResponse(presignedUploads.map { PresignedUploadResponse(it) }),
        )
    }
}
