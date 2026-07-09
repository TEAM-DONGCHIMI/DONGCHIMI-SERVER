# Architecture — 모듈 구조 & 레이어 규칙

> 이 문서는 모듈 구성, 레이어 간 참조 규칙, 모듈 간 의존성 관리를 다룬다.
> 설계/리뷰 작업, 새 모듈·도메인 추가 시 참조한다.

---

## 1. 모듈 구조

```
root
├── bootstrap/        # 실행 가능한 애플리케이션 모듈 (Spring Boot 진입점, 최종 조립)
├── api/              # Presentation 레이어 모듈 그룹 (Controller, Request, Response)
│   ├── core-api/     # role에 무관한 공통 인프라 (ApiResponse, GlobalExceptionHandler, WebMvcConfig 등)
│   ├── owner-api/    # OWNER role 전용 Controller
│   ├── admin-api/    # ADMIN role 전용 Controller
│   └── user-api/     # USER role 전용 Controller
├── core/             # 도메인 및 비즈니스 로직 모듈
├── gateway/          # 횡단관심사 모듈 그룹
│   ├── auth/         # 인증/인가 횡단관심사 모듈 (Spring Security, OAuth2)
│   └── logging/      # 로깅 횡단관심사 모듈 (MDC 기반 요청 추적)
├── infrastructure/   # 기술 구현 모듈
│   ├── db/           # DB 관련 모듈 (JPA Entity, Repository 구현체 등)
│   ├── client/       # 외부 API 클라이언트 모듈 (OAuth 등 HTTP Client 구현체)
│   ├── redis/        # Redis 모듈 (진행상태 스냅샷, pub/sub, 분산 신호 등)
│   └── excel/        # 엑셀 파싱 모듈 (Apache POI)
└── common/           # 공통 유틸리티 모듈
```

**모듈 간 의존 방향**

```
bootstrap → api:core-api
bootstrap → api:owner-api
bootstrap → api:admin-api
bootstrap → api:user-api
bootstrap → gateway:auth
bootstrap → gateway:logging
bootstrap → infrastructure:db
bootstrap → infrastructure:client
bootstrap → infrastructure:redis
bootstrap → infrastructure:excel
api:owner-api → api:core-api
api:admin-api → api:core-api
api:user-api → api:core-api
api:owner-api, api:admin-api, api:user-api, api:core-api → core
api:owner-api, api:admin-api, api:user-api, api:core-api → gateway:logging
api:owner-api, api:admin-api, api:user-api, api:core-api - - → gateway:auth   (runtimeOnly)
api:core-api → common
api:owner-api → common
gateway:auth → core
gateway:logging → core
infrastructure:db → core
infrastructure:client → core
infrastructure:redis → core
infrastructure:excel → core
core → common
```

- `bootstrap` 모듈은 `api:core-api`, `api:owner-api`, `api:admin-api`, `api:user-api`, `gateway:auth`, `gateway:logging`, `infrastructure:db`, `infrastructure:client`, `infrastructure:redis`, `infrastructure:excel`에 의존한다
- `api:owner-api`/`api:admin-api`/`api:user-api`는 각각 `api:core-api`에 의존한다 (`ApiUser` 인터페이스, `ApiResponse`, `GlobalExceptionHandler` 등 공통 인프라 재사용). role 모듈끼리는 서로 의존하지 않는다
- `api:core-api`를 포함한 모든 `api:*` 모듈은 `core`에 의존하고, `gateway:auth`는 `runtimeOnly`로 선언한다 (`PrincipalProvider` 구현체를 런타임에만 주입)
- `api:*` 모듈은 `gateway:logging`에 의존한다 (`GlobalExceptionHandler`에서 MDC 키 상수 참조)
- `api:*` 모듈은 필요 시 `common`에 의존할 수 있다 (순수 유틸리티 재사용 목적). `api:core-api`는 공통 인프라 구현에, `api:owner-api`는 `RegexPatterns`(이메일 형식 검증 등) 재사용에 사용한다. 모든 `api:*` 모듈이 의무적으로 의존해야 하는 것은 아니며, 실제로 유틸리티가 필요한 모듈만 추가한다
- role별 `HandlerMethodArgumentResolver`(`OwnerApiUserArgumentResolver` 등)는 각 role 모듈에 `@Component`로 선언하고, `api:core-api`의 `WebMvcConfig`가 컨텍스트에 존재하는 `HandlerMethodArgumentResolver` 빈을 전부 주입받아 등록한다 — `core-api`가 role 모듈을 직접 의존하지 않고도 role별 Resolver를 조립할 수 있다 (`bootstrap`이 4개 모듈을 모두 의존하고 `scanBasePackages = ["kr.dongchimi"]`로 전체를 스캔하기 때문)
- `gateway:auth` 모듈은 `core` 모듈에 의존한다
- `gateway:logging` 모듈은 `core` 모듈에 의존한다 (`PrincipalProvider`로 userId를 MDC에 주입)
- `infrastructure:db` 모듈은 `core` 모듈에 의존한다
- `infrastructure:client` 모듈은 `core` 모듈에 의존한다
- `infrastructure:redis` 모듈은 `core` 모듈에 의존한다
- `infrastructure:excel` 모듈은 `core` 모듈에 의존한다
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
| `bootstrap` | `api:core-api` + `api:owner-api` + `api:admin-api` + `api:user-api` + `gateway:auth` + `gateway:logging` + `infrastructure:db` + `infrastructure:redis` + `infrastructure:excel` + Spring Boot 실행 관련 (Actuator 등) |
| `api:core-api` | `common` + `core` + `gateway:logging` + `gateway:auth`(runtimeOnly) + Spring MVC 관련 라이브러리 |
| `api:owner-api` / `api:admin-api` / `api:user-api` | `api:core-api` + `core` + `gateway:logging` + `gateway:auth`(runtimeOnly) + `common`(선택, 유틸리티 재사용 시) + Spring MVC 관련 라이브러리 |
| `core` | 순수 Kotlin / 비즈니스 로직 라이브러리만 |
| `gateway:auth` | `core` + Spring Security / OAuth2 관련 라이브러리 |
| `gateway:logging` | `core` + `spring-web` + `spring-context` + `slf4j-api` + kotlin-logging + Servlet API |
| `infrastructure:db` | `core` + 기술 구현 라이브러리 (JPA, H2 Console 등) |
| `infrastructure:client` | `core` + 외부 통신 라이브러리 (spring-web / RestClient, Jackson 등) |
| `infrastructure:redis` | `core` + `spring-boot-starter-data-redis` 등 Redis 클라이언트 라이브러리 |
| `infrastructure:excel` | `core` + `poi-ooxml` 등 엑셀 파싱 라이브러리 |
| `common` | 순수 Kotlin / 유틸리티만 |
