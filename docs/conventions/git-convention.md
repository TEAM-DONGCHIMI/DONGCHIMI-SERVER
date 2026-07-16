# Git Convention

> 이 문서는 커밋 메시지, 브랜치 전략, PR/이슈 작성 규칙을 다룬다.

---

## 1. 커밋 메시지

### 1-1. 형식

**일반 커밋**

```
type: 제목
```

**PR Squash Merge 커밋**

```
type: 제목 - #PR번호
```

- **type**: 변경 성격을 나타내는 접두사 (아래 표 참고)
- **제목**: 한글로 작성, 명사형 또는 동사 종결형, 마침표 없음
- **PR번호**: Squash Merge 커밋에만 ` - #PR번호` 형식으로 붙인다

### 1-2. Type 종류

| type | 설명 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변경 없는 코드 개선 |
| `docs` | 문서 추가·수정 |
| `test` | 테스트 코드 추가·수정 |
| `chore` | 빌드 설정, 의존성, 기타 잡무 |
| `init` | 프로젝트·모듈 초기 세팅 |

### 1-3. 예시

**일반 커밋**

```
feat: 유저 로그인 API 추가
fix: 토큰 만료 시 NPE 발생 수정
refactor: UserService 레이어 분리
docs: git 컨벤션 문서 추가
chore: spring-boot 버전 3.4.0으로 업그레이드
```

**PR Squash Merge 커밋**

```
feat: 유저 로그인 API 추가 - #12
fix: 토큰 만료 시 NPE 발생 수정 - #34
```

---

## 2. 브랜치 전략

### 2-1. 형식

```
{type}/#{이슈번호}-{작업내용}
```

- **type**: 커밋 type과 동일하게 사용한다
- **이슈번호**: `#`을 붙여 작성한다
- **작업내용**: 영문 소문자와 하이픈(`-`)만 사용한다

### 2-2. 브랜치 종류

| 브랜치 | 설명 |
| --- | --- |
| `main` | 배포 기준 브랜치 |
| `feat/#{이슈번호}-{작업내용}` | 기능 개발 |
| `fix/#{이슈번호}-{작업내용}` | 버그 수정 |
| `refactor/#{이슈번호}-{작업내용}` | 리팩토링 |
| `docs/#{이슈번호}-{작업내용}` | 문서 작업 |
| `chore/#{이슈번호}-{작업내용}` | 빌드·설정 변경 |

### 2-3. 브랜치 네이밍 예시

```
feat/#12-user-login
fix/#34-token-expired-npe
refactor/#7-user-service-layer
docs/#5-git-convention
```

---

## 3. PR 규칙

### 3-1. PR 제목

```
[{Type}/#{이슈번호}] {설명}
```

- **Type**: 커밋 type과 동일하되 첫 글자를 대문자로 작성한다 (`Feat`, `Fix`, `Refactor`, `Docs`, `Test`, `Chore`, `Init`)
- **이슈번호**: 연관 이슈번호를 반드시 포함한다
- **설명**: 한글로 간략히 작성한다

```
[Feat/#12] 유저 로그인 API 추가
[Fix/#34] 토큰 만료 시 NPE 발생 수정
[Refactor/#7] UserService 레이어 분리
```

Squash Merge 시 커밋 메시지에 `- #PR번호`를 추가한다.

### 3-2. PR 본문

`.github/pull_request_template.md` 템플릿을 사용한다.

- **연관 이슈**: 반드시 연결한다
- **작업 내용**: 변경 사항을 간략히 기술한다
- **리뷰 요구사항**: 리뷰어에게 특별히 확인 요청할 내용이 있으면 작성한다

### 3-3. Merge 전략

- `Squash and Merge`를 기본으로 사용한다
- PR 단위로 커밋이 하나로 정리되어 `main` 이력이 깔끔하게 유지된다

### 3-4. 기타 규칙

- `main`에 직접 push하지 않는다
- PR은 최소 1명의 리뷰 승인 후 Merge한다
- Merge 후 작업 브랜치는 삭제한다

---

## 4. 배포 규칙

- `main` push → `deploy-dev.yml` 자동 실행 → **dev 서버**에 배포된다
- **prod 배포는 GitHub Release 발행(published)으로 트리거**한다 (`deploy-prod.yml`)
  - 릴리스 태그: `vX.Y.Z` (Semantic Versioning)
  - 릴리스 대상 커밋은 반드시 dev에 먼저 배포되어 검증이 끝난 `main`의 커밋이어야 한다 (Flyway 마이그레이션 순서 보장)
  - 릴리스 노트에는 포함된 주요 변경사항(머지된 PR 목록)을 간단히 기술한다

```
PR 머지 → main push → deploy-dev.yml → dev 배포·검증
                                          ↓
                    GitHub Release 발행(vX.Y.Z) → deploy-prod.yml → prod 배포
```

---

## 5. 이슈 규칙

### 5-1. 이슈 생성

`.github/ISSUE_TEMPLATE/-issue-template.md` 템플릿을 사용한다.

- 구현할 내용을 체크리스트로 작성한다
- 참고 자료가 있으면 함께 첨부한다

### 5-2. 이슈-브랜치-PR 흐름

```
이슈 생성 → 브랜치 생성 (이슈번호 포함) → 작업 및 커밋 → PR 생성 (이슈 연결) → 리뷰 → Merge → 브랜치 삭제
```
