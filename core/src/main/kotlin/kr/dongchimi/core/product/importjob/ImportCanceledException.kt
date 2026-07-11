package kr.dongchimi.core.product.importjob

/**
 * 워커가 체크포인트에서 취소 신호를 발견했을 때만 던지는 내부 제어 흐름 신호다.
 * API 경계를 넘지 않으므로(ImportJobProcessor 안에서만 잡는다) CoreException을 상속하지 않는다.
 */
class ImportCanceledException : RuntimeException("분석이 취소됐습니다")
