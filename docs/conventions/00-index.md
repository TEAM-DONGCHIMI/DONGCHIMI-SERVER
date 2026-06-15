# Kotlin + 멀티모듈 컨벤션 — 인덱스

프로젝트의 Kotlin + Spring Boot 멀티모듈 컨벤션을 용도별로 5개 문서로 분리했다.
작업 종류에 맞는 문서만 읽으면 된다.

| 문서 | 다루는 내용 | 언제 참조하는가 |
| --- | --- | --- |
| [`architecture.md`](./architecture.md) | 모듈 구조(`api`/`core`/`infrastructure:db`/`common`), 모듈 간 의존 방향, 레이어 구조 및 참조 규칙, gradle 의존성 관리 | 새 모듈/도메인 설계, 의존성 구조 리뷰 |
| [`coding-style.md`](./coding-style.md) | 레이어별 클래스 네이밍, 도메인 객체/DTO/VO/JPA Entity/Repository/Implement Layer(Reader·Appender·Manager 등)/Service 작성 패턴, Validation 규칙 | Controller, Service, Repository, DTO 등 실제 코드 작성/리뷰 |
| [`error-handling.md`](./error-handling.md) | `ErrorCode`, `CustomException` 계층, `GlobalExceptionHandler`, `@ApiErrorCode` 기반 Swagger 에러 문서화 | 새 에러 코드 추가, 예외 처리 로직 작업 |
| [`config-and-auth.md`](./config-and-auth.md) | `@ConfigurationProperties` 설정 바인딩, `PrincipalProvider` 인증 흐름 및 사용법 | 설정값 추가, 인증이 필요한 API 작업 |
| [`flyway-migration.md`](./flyway-migration.md) | Flyway 마이그레이션 파일 위치/네이밍, 테이블·컬럼·인덱스 작성 규칙, JPA `ddl-auto` 설정 | 테이블/컬럼 추가·변경, 새 도메인 스키마 작업 |

## 빠른 참조

- "User 같은 새 도메인 추가하고 싶어" → `architecture.md` (모듈/레이어 위치 확인) → `coding-style.md` (클래스 작성) → `flyway-migration.md` (테이블 생성)
- "에러 코드 하나 추가해야 해" → `error-handling.md`
- "로그인한 유저 정보로 API 만들어줘" → `config-and-auth.md`
- "이 도메인은 단순해서 Manager로 묶고 싶어" → `coding-style.md` 2-5절
- "테이블에 컬럼 하나 추가해야 해" → `flyway-migration.md` 3-3절
