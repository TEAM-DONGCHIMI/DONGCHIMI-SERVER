package kr.dongchimi.db.common

import kr.dongchimi.core.common.PageOffset
import org.springframework.data.domain.PageRequest

fun PageOffset.toPageRequest(): PageRequest = PageRequest.of(page, size)
