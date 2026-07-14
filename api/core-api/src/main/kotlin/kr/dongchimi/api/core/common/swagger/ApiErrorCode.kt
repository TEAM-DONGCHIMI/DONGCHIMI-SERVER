package kr.dongchimi.api.core.common.swagger

import kr.dongchimi.core.common.exception.ErrorCode
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable
annotation class ApiErrorCode(
    val type: KClass<out ErrorCode>,
    vararg val codes: String,
)
