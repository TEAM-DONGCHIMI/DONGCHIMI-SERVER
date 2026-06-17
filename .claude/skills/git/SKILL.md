---
name: git
description: docs/conventions/git-convention.md를 참고해 커밋, 브랜치, 이슈, PR 등 git 관련 액션을 수행하는 스킬.
---

# Git 액션 워크플로우

git 관련 작업 전에 이 워크플로우를 따른다.

## Step 1 — 컨벤션 읽기

반드시 `docs/conventions/git-convention.md`를 읽고 시작한다.

```
Read: docs/conventions/git-convention.md
```

## Step 2 — 작업 유형 파악 및 실행

요청 내용에 따라 아래 액션을 수행한다.

### 커밋

- 형식: `type: 제목`
- 제목은 한글, 마침표 없음
- `Co-Authored-By` 트레일러를 추가하지 않는다
- 작업 단위별로 커밋을 나눈다

### 이슈 생성

- `.github/ISSUE_TEMPLATE/-issue-template.md` 형식을 따른다
- 구현 내용을 체크리스트로 작성한다

### 브랜치 생성

- 형식: `{type}/#{이슈번호}-{작업내용(영어)}`
- 반드시 이슈를 먼저 생성해 이슈번호를 확보한 뒤 브랜치를 만든다
- `main`에 직접 커밋이 쌓인 경우: 브랜치 생성 → main을 `origin/main`으로 리셋 → 브랜치 푸시

### PR 생성

- 제목 형식: `[{Type}/#{이슈번호}] {설명}`
- `.github/pull_request_template.md` 형식을 따른다
- 연관 이슈를 반드시 연결한다

### 이슈-브랜치-PR 전체 흐름

```
이슈 생성 → 브랜치 생성 (이슈번호 포함) → 작업 및 커밋 → PR 생성 (이슈 연결) → 리뷰 → Merge → 브랜치 삭제
```

## Step 3 — 자가 검증

- [ ] 커밋 메시지 type이 올바른가
- [ ] 브랜치명에 이슈번호가 포함됐는가
- [ ] PR 제목 형식이 `[{Type}/#{이슈번호}] {설명}`인가
- [ ] PR에 연관 이슈가 연결됐는가
- [ ] `Co-Authored-By` 트레일러가 없는가
