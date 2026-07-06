package kr.dongchimi.api.core.swagger

import kr.dongchimi.core.common.exception.ErrorCode
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiErrorCodes(
    vararg val value: KClass<out ErrorCode>,
)
