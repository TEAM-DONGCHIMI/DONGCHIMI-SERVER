# Architecture — 모듈 구조 & 레이어 규칙

> 이 문서는 모듈 구성, 레이어 간 참조 규칙, 모듈 간 의존성 관리를 다룬다.
> 설계/리뷰 작업, 새 모듈·도메인 추가 시 참조한다.

---

## 1. 모듈 구조

```
root
├── api/              # 실행 가능한 애플리케이션 모듈 (Spring Boot 진입점)
├── core/             # 도메인 및 비즈니스 로직 모듈
├── infrastructure/   # 기술 구현 모듈
│   └── db/           # DB 관련 모듈 (JPA Entity, Repository 구현체 등)
└── common/           # 공통 유틸리티 모듈
```

**모듈 간 의존 방향**

```
api → core
api → infrastructure:db
infrastructure:db → core
core → common
```

- `api` 모듈은 `core`, `infrastructure:db` 모듈에 의존한다
- `infrastructure:db` 모듈은 `core` 모듈에 의존한다
- `core` 모듈은 다른 모듈에 의존하지 않는다 (단, `common` 제외)
- `common` 모듈은 어떤 모듈에도 의존하지 않는다

---

## 2. 레이어 구조

각 도메인 모듈 내부는 아래 4개의 레이어로 구성한다.

```
Presentation Layer   (Controller, Request, Response)
        ↓
Business Layer       (Service)
        ↓
Implement Layer      (Reader, Store, Remover, Processor, Validator, Manager, ...)
        ↓
Data Access Layer    (Repository Interface, Client Interface)
```

---

## 3. 레이어 규칙

1. **레이어는 위에서 아래 방향으로만 참조한다**
2. **역방향 참조를 금지한다** — Implement Layer가 Business Layer를 참조하지 않는다
3. **레이어를 건너뛰는 참조를 금지한다** — Business Layer가 Repository를 직접 참조하지 않는다
4. **동일 레이어 간 참조를 금지한다** — 단, Implement Layer 내부에서는 상호 참조를 허용한다

---

## 4. 의존성 관리

### 4-1. `implementation` vs `api`

- 모듈 간 의존성은 원칙적으로 `implementation`을 사용한다
- `api`를 사용하면 의존성이 상위 모듈로 전파되어 레이어 오염이 발생할 수 있다

```kotlin
// infrastructure:db 모듈 build.gradle.kts
dependencies {
    implementation(project(":core"))  // ✅ core 모듈 의존
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")  // ✅ JPA는 infrastructure:db 내부에만
}

// core 모듈 build.gradle.kts
dependencies {
    // JPA 의존성 없음 ✅
}
```

### 4-2. 모듈별 의존성 원칙

| 모듈 | 허용 의존성 |
| --- | --- |
| `core` | 순수 Kotlin / 비즈니스 로직 라이브러리만 |
| `infrastructure:db` | `core` + 기술 구현 라이브러리 (JPA, Redis, HTTP Client 등) |
| `api` | `core` + `infrastructure:db` + Spring Boot 실행 관련 |
| `common` | 순수 Kotlin / 유틸리티만 |
