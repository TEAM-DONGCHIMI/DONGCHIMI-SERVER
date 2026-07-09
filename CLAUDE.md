# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 작업 원칙

**구현 전에 확실하지 않은 것은 반드시 사용자에게 질문한다.**

- 요구사항이 여러 방식으로 해석될 수 있으면 임의로 선택하지 말고 선택지를 제시하고 물어본다.
- 컨벤션 문서에 없는 새로운 패턴을 도입해야 할 때, 어느 모듈/레이어에 둘지 애매할 때도 먼저 확인한다.
- 요청받은 범위만 수정한다. 인접 코드 개선·리팩토링은 임의로 하지 않는다.

## 프로젝트 개요

Kotlin 2.3 + Spring Boot 4.1 멀티모듈 서버 (Java 21 toolchain). 패키지 루트는 `kr.dongchimi`.

## 명령어

```bash
./gradlew build                  # 전체 빌드 (ktlint 검사 포함)
./gradlew test                   # 전체 테스트
./gradlew :core:test             # 특정 모듈 테스트
./gradlew :core:test --tests "kr.dongchimi.core.user.UserServiceTest"  # 단일 테스트
./gradlew ktlintCheck            # 린트 검사
./gradlew ktlintFormat           # 린트 자동 수정 (pre-commit 훅이 자동 실행함)
./gradlew :bootstrap:bootRun     # 애플리케이션 실행
```


## 컨벤션 문서 (필독)

상세 컨벤션은 `docs/conventions/`에 있다. **코드를 작성하기 전에 작업 종류에 맞는 문서를 반드시 읽는다.** `00-index.md`에 작업별 참조 가이드가 있다.

| 작업 | 문서 |
| --- | --- |
| 새 모듈/도메인 설계, 의존성 | `architecture.md` |
| Controller/Service/Repository/DTO 작성 | `coding-style.md` |
| 에러 코드, 예외 처리 | `error-handling.md` |
| 설정값 추가, 인증 API | `config-and-auth.md` |
| 로깅, MDC | `logging.md` |
| 테이블/컬럼 변경 | `flyway-migration.md` |
| 커밋, 브랜치, PR | `git-convention.md` |

## 아키텍처 핵심 규칙

### 모듈 구조와 의존 방향

```
bootstrap → api:{core,owner,admin,user}-api, gateway:{auth,logging}, infrastructure:{db,redis,excel}
api:{owner,admin,user}-api → api:core-api → core
gateway:auth, gateway:logging, infrastructure:{db,redis,excel} → core
core → common (core는 그 외 어떤 모듈에도 의존하지 않음)
```

- `core`: 도메인 객체, Service, Implement Layer, Repository **인터페이스**. 순수 Kotlin — Spring Security/JPA 의존성 금지.
- `infrastructure:db`: JPA Entity(`{Domain}JpaEntity`), `{Domain}JpaRepository`, `{Domain}RepositoryImpl`, Flyway 마이그레이션(`src/main/resources/db/migration/`).
- `api:*`: role별(OWNER/ADMIN/USER) Controller 모듈로 분리. 공통 인프라(ApiResponse, GlobalExceptionHandler, WebMvcConfig)는 `api:core-api`. role 모듈끼리는 서로 의존하지 않는다.
- 모듈 간 의존성은 `implementation`을 사용한다 (`api` 전파 금지). `gateway:auth`는 api 모듈에서 `runtimeOnly`.

### 레이어 규칙

```
Presentation (Controller) → Business (Service) → Implement (Reader/Appender/...) → Data Access (Repository)
```

- 위→아래 단방향 참조만 허용. 레이어 건너뛰기 금지 — **Service는 Repository를 직접 참조하지 않고** Implement Layer를 통해서만 접근한다.
- Implement Layer는 존재해야 한다는 원칙만 지키면 되고, 세부 분리 방식·네이밍은 도메인 특성에 따라 유연하게 정한다 (`Reader`/`Appender`/`Manager` 등은 예시일 뿐 강제가 아님 — `coding-style.md` 2-6절 참고).

### 자주 틀리는 규칙

- Controller → Service 전달 시 Request DTO를 그대로 넘기지 않는다. `request.toCommand()`로 VO(`{Domain}{Action}Command`)로 변환해 전달한다.
- 도메인 객체는 `data class`, JPA 어노테이션 금지. JPA Entity는 `infrastructure:db`에 두고 `toDomain()` 제공, Base Entity(`BaseTimeEntity` 등) 상속.
- 예외는 `CoreException` + `ErrorCode` enum (도메인별 `{Domain}ErrorCode`). Spring `HttpStatus` 대신 `common`의 `ErrorStatus` 상수 사용.
- 설정은 `@ConfigurationProperties` + `data class`. `@Value` 금지. 모듈별 yml(`application-{모듈명}.yml`)에 정의하고 bootstrap의 `application.yml`에서 import. `${ENV_VAR}` 추가 시 루트 `.env.example`도 동기화.
- JPA Entity 필드 변경은 반드시 Flyway 마이그레이션 파일과 함께. 이미 머지된 마이그레이션 파일은 수정하지 않는다. `ddl-auto`는 `validate`/`none`.
- 인증 필요한 Controller는 자기 role 모듈의 `ApiUser` 구현체(예: `OwnerApiUser`)를 파라미터로 선언하면 자동 주입된다. Service에는 `ApiUser`가 아닌 `userId: Long`을 전달한다.
- Validation 메시지와 에러 메시지는 한글로 작성한다.

## Git

- 커밋: `type: 한글 제목` (type: `feat`/`fix`/`refactor`/`docs`/`test`/`chore`/`init`)
- 브랜치: `{type}/#{이슈번호}-{작업내용}` (예: `feat/#12-user-login`)
- PR 제목: `[{Type}/#{이슈번호}] 설명`, Squash Merge 기본, `main` 직접 push 금지
