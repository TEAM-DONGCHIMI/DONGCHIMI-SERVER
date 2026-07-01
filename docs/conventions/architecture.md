# Architecture — 모듈 구조 & 레이어 규칙

> 이 문서는 모듈 구성, 레이어 간 참조 규칙, 모듈 간 의존성 관리를 다룬다.
> 설계/리뷰 작업, 새 모듈·도메인 추가 시 참조한다.

---

## 1. 모듈 구조

```
root
├── bootstrap/        # 실행 가능한 애플리케이션 모듈 (Spring Boot 진입점, 최종 조립)
├── api/              # Presentation 레이어 모듈 (Controller, Request, Response)
├── core/             # 도메인 및 비즈니스 로직 모듈
├── gateway/          # 횡단관심사 모듈 그룹
│   ├── auth/         # 인증/인가 횡단관심사 모듈 (Spring Security, OAuth2)
│   └── logging/      # 로깅 횡단관심사 모듈 (MDC 기반 요청 추적)
├── infrastructure/   # 기술 구현 모듈
│   └── db/           # DB 관련 모듈 (JPA Entity, Repository 구현체 등)
└── common/           # 공통 유틸리티 모듈
```

**모듈 간 의존 방향**

```
bootstrap → api
bootstrap → gateway:auth
bootstrap → gateway:logging
bootstrap → infrastructure:db
api → core
api → gateway:logging
api - - → gateway:auth   (runtimeOnly)
gateway:auth → core
gateway:logging → core
infrastructure:db → core
core → common
```

- `bootstrap` 모듈은 `api`, `gateway:auth`, `gateway:logging`, `infrastructure:db`에 의존한다
- `api` 모듈은 `core`에 의존하고, `gateway:auth`는 `runtimeOnly`로 선언한다 (`PrincipalProvider` 구현체를 런타임에만 주입)
- `api` 모듈은 `gateway:logging`에 의존한다 (`GlobalExceptionHandler`에서 MDC 키 상수 참조)
- `gateway:auth` 모듈은 `core` 모듈에 의존한다
- `gateway:logging` 모듈은 `core` 모듈에 의존한다 (`PrincipalProvider`로 userId를 MDC에 주입)
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

// gateway-auth 모듈 build.gradle.kts
dependencies {
    implementation(project(":core"))  // ✅ core 모듈 의존
    implementation("org.springframework.boot:spring-boot-starter-security")  // ✅ Security는 gateway-auth 내부에만
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
}

// core 모듈 build.gradle.kts
dependencies {
    // Security / JPA 의존성 없음 ✅
}
```

### 4-2. 모듈별 의존성 원칙

| 모듈 | 허용 의존성 |
| --- | --- |
| `bootstrap` | `api` + `gateway:auth` + `gateway:logging` + `infrastructure:db` + Spring Boot 실행 관련 (Actuator 등) |
| `api` | `core` + `gateway:logging` + `gateway:auth`(runtimeOnly) + Spring MVC 관련 라이브러리 |
| `core` | 순수 Kotlin / 비즈니스 로직 라이브러리만 |
| `gateway:auth` | `core` + Spring Security / OAuth2 관련 라이브러리 |
| `gateway:logging` | `core` + Servlet API + kotlin-logging |
| `infrastructure:db` | `core` + 기술 구현 라이브러리 (JPA, H2 Console, Redis, HTTP Client 등) |
| `common` | 순수 Kotlin / 유틸리티만 |
