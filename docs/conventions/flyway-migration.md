# Flyway Migration — DB 마이그레이션 컨벤션

> 이 문서는 Flyway를 사용한 DB 스키마 마이그레이션 파일의 위치, 네이밍, 작성 규칙을 다룬다.
> 테이블/컬럼 추가·변경, 새 도메인의 스키마 작업 시 참조한다.

---

## 1. 위치

- 마이그레이션 파일은 `infrastructure:db` 모듈에 위치한다 (`coding-style.md`의 JPA Entity와 동일 모듈)

```
infrastructure/db/src/main/resources/db/migration/
├── V1__init.sql
├── V2__create_user_table.sql
├── V3__add_status_to_user.sql
└── ...
```

---

## 2. 파일 네이밍

- Flyway 기본 규칙을 따른다: `V{버전}__{설명}.sql`
- 버전 번호는 순차 증가하는 정수를 사용한다 (`V1`, `V2`, `V3`, ...)
- 설명은 영문 snake_case로 작성하며, 변경 내용을 동사로 시작해 명확히 표현한다
  - 테이블 생성: `create_{table}_table`
  - 컬럼 추가: `add_{column}_to_{table}`
  - 컬럼 삭제: `drop_{column}_from_{table}`
  - 컬럼/타입 변경: `alter_{column}_in_{table}`
  - 인덱스 추가: `add_index_to_{table}`
  - 데이터 마이그레이션: `migrate_{table}_data`

```
V4__add_status_to_user.sql
V5__create_post_table.sql
V6__add_index_to_post_author_id.sql
```

- **버전 번호 충돌 주의**: 여러 브랜치에서 동시에 작업할 경우 머지 직전 버전 번호가 겹치지 않는지 확인한다. 겹치면 머지하는 쪽에서 번호를 재조정한다.

---

## 3. 작성 규칙

### 3-1. 불변성

- 이미 머지되어 적용된 마이그레이션 파일은 **수정하지 않는다**
- 변경이 필요하면 새 버전의 마이그레이션 파일을 추가한다

### 3-2. 테이블 생성

- 테이블명은 복수형 snake_case를 사용한다 (`users`, `posts`)
- 기본 컬럼(`id`, `created_at`, `updated_at`)을 포함한다

```sql
-- V5__create_post_table.sql
CREATE TABLE posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3-3. 컬럼 추가/변경

- 운영 데이터가 존재하는 테이블에 `NOT NULL` 컬럼을 추가할 때는 `DEFAULT` 값을 지정한다

```sql
-- V6__add_status_to_post.sql
ALTER TABLE posts
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
```

### 3-4. 인덱스

- 조회 빈도가 높은 외래키 컬럼, WHERE/ORDER BY에 자주 사용되는 컬럼에는 인덱스를 추가한다
- 인덱스명은 `idx_{table}_{column}` 형식을 사용한다

```sql
-- V7__add_index_to_post_author_id.sql
CREATE INDEX idx_posts_author_id ON posts (author_id);
```

### 3-5. 데이터 마이그레이션

- 스키마 변경과 데이터 마이그레이션은 가능하면 별도 파일로 분리한다
- 대량 데이터 변경은 운영 환경에서의 실행 시간/락 영향을 고려해 별도로 검토한다

---

## 4. JPA Entity와의 관계

- `UserJpaEntity` 등 JPA Entity의 필드 변경은 반드시 대응하는 Flyway 마이그레이션 파일과 함께 작성한다 (Entity 작성 규칙은 `coding-style.md` 2-3절 참고)
- `ddl-auto`는 `validate` 또는 `none`으로 설정하고, 스키마 변경은 Flyway로만 관리한다

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration
```

---

## 5. 로컬/테스트 환경

- 로컬 개발 환경에서도 동일한 마이그레이션 파일을 적용해 운영 환경과 스키마 일치를 보장한다
- 테스트 환경에서는 `clean` 후 전체 마이그레이션을 재실행하여 스키마 drift를 방지한다 (CI에서는 매 빌드마다 클린 마이그레이션 권장)
