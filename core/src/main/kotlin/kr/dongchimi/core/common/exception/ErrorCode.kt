package kr.dongchimi.core.common.exception

interface ErrorCode {
    val name: String
    val status: Int
    val message: String
}