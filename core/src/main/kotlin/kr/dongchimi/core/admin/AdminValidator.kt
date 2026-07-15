package kr.dongchimi.core.admin

import kr.dongchimi.core.common.exception.CoreException
import org.springframework.stereotype.Component

@Component
class AdminValidator(
    private val adminReader: AdminReader,
) {
    /** 관리자 계정이 존재하지 않으면 401을 던진다. */
    fun validate(adminId: Long) {
        if (!adminReader.existsById(adminId)) throw CoreException(AdminErrorCode.ADMIN_NOT_FOUND)
    }
}
