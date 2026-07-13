package kr.dongchimi.core.admin

import org.springframework.stereotype.Service

@Service
class AdminService(
    private val adminReader: AdminReader,
) {
    fun findByIds(ids: Set<Long>): List<Admin> = adminReader.readAllByIds(ids)
}
