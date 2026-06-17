---
name: convention
description: 이 프로젝트의 코딩 컨벤션을 적용하는 스킬. 코드를 작성하거나 리뷰할 때 /convention으로 호출하면 docs/conventions/ 문서를 읽고 컨벤션에 맞는 코드를 작성한다.
---

# Convention 적용 워크플로우

코딩 작업 전에 이 워크플로우를 따라 컨벤션을 파악하고 적용한다.

## Step 1 — 인덱스 읽기

반드시 `docs/conventions/00-index.md`를 먼저 읽는다.
인덱스에는 작업 유형별로 참조해야 할 문서가 정리되어 있다.

```
Read: docs/conventions/00-index.md
```

## Step 2 — 필요한 문서만 선택적으로 읽기

현재 작업 컨텍스트를 바탕으로 인덱스의 "언제 참조하는가" 열을 보고 필요한 문서만 읽는다.
모든 문서를 한 번에 읽지 않는다.

| 작업 유형 | 읽을 문서 |
|-----------|-----------|
| Controller / Service / Repository / DTO 작성 | `coding-style.md` |
| 새 모듈·도메인 설계, 의존성 구조 | `architecture.md` |
| 새 에러 코드 추가, 예외 처리 | `error-handling.md` |
| 인증이 필요한 API, 설정값 추가 | `config-and-auth.md` |
| 테이블·컬럼 추가·변경 | `flyway-migration.md` |

인덱스 하단의 "빠른 참조" 섹션도 활용한다.

## Step 3 — 컨벤션 적용 및 자가 검증

코드 작성 완료 후 아래 항목을 확인한다.

- [ ] 레이어별 클래스 명명 규칙을 지켰는가 (`coding-style.md`)
- [ ] 의존성 방향이 단방향인가 — 레이어 스킵, 역방향 참조 없는가 (`architecture.md`)
- [ ] 에러는 `ErrorCode` + `CustomException` 계층을 통해 처리했는가 (`error-handling.md`)
- [ ] 인증 필요 API에서 `PrincipalProvider`를 올바르게 사용했는가 (`config-and-auth.md`)
- [ ] DB 변경 시 Flyway 마이그레이션 파일을 함께 작성했는가 (`flyway-migration.md`)
