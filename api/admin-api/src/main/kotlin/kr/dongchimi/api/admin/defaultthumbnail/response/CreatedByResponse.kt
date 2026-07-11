package kr.dongchimi.api.admin.defaultthumbnail.response

import io.swagger.v3.oas.annotations.media.Schema

data class CreatedByResponse(
    @Schema(description = "관리자 id")
    val adminId: Long,
    @Schema(description = "관리자 이름")
    val name: String,
    @Schema(description = "관리자 이메일")
    val email: String,
)
