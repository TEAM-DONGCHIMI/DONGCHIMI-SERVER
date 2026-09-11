# DONGCHIMI - 우리 동네 마트의 특가 소식을 한곳에 모은 전단 플랫폼

**📌 Repository |** [TEAM-DONGCHIMI](https://github.com/TEAM-DONGCHIMI)

<br></br>

## 📋 Introduction

### 🛒 아직도 종이 전단으로 특가 소식 알리세요? 동치미로 더 쉽고 빠르게!

동네 마트들은 여전히 종이 전단과 매장 앞 입간판으로 할인 소식을 알리고 있습니다. <br>
점주는 전단을 만들고 배포하는 데 시간과 비용을 쓰고, 소비자는 우리 동네 마트에서 지금 무엇이 싼지 알기 어렵죠 😭

동치미는 이러한 문제를 해결하기 위해 탄생했습니다. <br>
**마트 점주는 상품과 특가를 손쉽게 등록하고, 사용자는 위치 기반으로 주변 마트의 특가 상품을 한눈에 조회하는 플랫폼 DONGCHIMI**를 소개합니다!

- **마트 관리**: 마트 정보 등록·수정, 휴무일 관리까지 점주가 한곳에서 처리할 수 있습니다.
- **상품 관리**: 오늘의 특가, 기간 할인 등 상품을 등록·수정하고 임시저장 후 한 번에 확정할 수 있습니다.
- **엑셀 일괄 등록**: 엑셀 파일 하나만 올리면 AI가 상품을 분석해 카테고리 분류와 이미지 매칭까지 자동으로 처리하고, 진행 상황을 실시간(SSE)으로 보여줍니다.
- **주변 마트 조회**: 사용자의 현재 위치를 기준으로 주변 마트와 특가 상품을 조회할 수 있습니다.

</br></br>

## ✨ Main Feature

### 점주 (Owner)
- 카카오 소셜 로그인 / Refresh Token 기반 로그인 유지 / 로그아웃
- 마트 정보 등록·수정, 휴무일 관리
- 상품 등록·수정·삭제, 오늘의 특가·기간 할인 관리
- 엑셀 업로드 → AI(Gemini) 분석 → 임시저장 → 최종 확정으로 이어지는 상품 일괄 등록 파이프라인
  - 코루틴 기반 비동기 워커 + DB 작업 큐(`FOR UPDATE SKIP LOCKED`)로 다중 인스턴스에서도 안전하게 처리
  - SSE 실시간 진행 상태 스트리밍, 분석 취소 지원
- 점주 홈 화면 (마트·상품 현황 조회)

### 사용자 (User)
- 위치 기반 주변 마트·특가 상품 조회 (PostGIS 공간 쿼리)
- 상품 상세 조회, 조회수 집계 (Redis write-back)

### 관리자 (Admin)
- 관리자 회원가입·로그인
- 기본 썸네일 이미지 관리 (일괄 등록, 수정, 커서 기반 목록 조회)
- Presigned URL 기반 S3 이미지 업로드 (confirm / rollback)

### 공통
- 미처리 예외 발생 시 Discord 웹훅으로 에러 알림 (ErrorNotifier)
- 요청 로깅 및 MDC 기반 추적

</br></br>

## 🧑🏻‍💻 Developers

|                              황수민                               |                            김채현                            |
|:--------------------------------------------------------------:|:----------------------------------------------------------:|
| <img width="300" alt="image" src="https://github.com/user-attachments/assets/9fe2dd3f-2535-4266-99be-02b6b20008aa" /> |  <img width="300" alt="image" src="https://github.com/user-attachments/assets/030bef38-831b-4008-91e0-f0ad7e5fbdb1" /> |
|            [tnals0924](https://github.com/tnals0924)            |           [imddoy](https://github.com/imddoy)              |
| 멀티모듈 아키텍처 및 role별 API 모듈 분리 <br> 엑셀 상품 일괄 등록 비동기 분석 파이프라인 <br> Gemini AI 카테고리 분류·이미지 매칭 <br> 위치 기반 마트 조회 (PostGIS) <br> 조회수 집계 (Redis write-back) <br> 미처리 예외 모니터링 (ErrorNotifier) <br> k6 성능 테스트 및 저사양 인프라 튜닝 | 점주 인증 (카카오 로그인, Refresh Token, 로그아웃) <br> 마트 등록·수정 API <br> 상품·특가 API <br> 관리자 회원가입·로그인 <br> 기본 썸네일 관리 API <br> Presigned URL 이미지 업로드 (S3) |

</br></br>

## 🤝 Convention

- [Git Convention](docs/conventions/git-convention.md)
- [Code Convention](docs/conventions/coding-style.md)
- [Architecture Convention](docs/conventions/architecture.md)
- [Error Handling](docs/conventions/error-handling.md)
- 전체 컨벤션 문서: [docs/conventions](docs/conventions/00-index.md)

</br></br>

## 🖥️ Tech Stack

### Language & Framework

<img src="https://img.shields.io/badge/Kotlin_2.3-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin badge"> <img src="https://img.shields.io/badge/Spring_Boot_4.1-6DB33F?style=flat-square&logo=spring-boot&logoColor=white" alt="Spring Boot badge"> <img src="https://img.shields.io/badge/Java_21-437291?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21 badge"> <img src="https://img.shields.io/badge/Gradle-02303A?style=flat-square&logo=gradle&logoColor=white" alt="Gradle badge"> <img src="https://img.shields.io/badge/Coroutines-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin Coroutines badge">

#### ORM & Migration

<img src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square&logo=Databricks&logoColor=white" alt="Spring Data JPA badge"> <img src="https://img.shields.io/badge/Flyway-CC0200?style=flat-square&logo=flyway&logoColor=white" alt="Flyway badge">

#### Authorization

<img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white" alt="Spring Security badge"> <img src="https://img.shields.io/badge/JSON_Web_Tokens-000000?style=flat-square&logo=JSON%20Web%20Tokens&logoColor=white" alt="JWT badge"> <img src="https://img.shields.io/badge/Kakao_OAuth2-FFCD00?style=flat-square&logo=kakaotalk&logoColor=black" alt="Kakao OAuth badge">

#### Database

<img src="https://img.shields.io/badge/PostgreSQL_(PostGIS)-4169E1?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL badge"> <img src="https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white" alt="Redis badge">

#### AI

<img src="https://img.shields.io/badge/Google_Gemini-8E75B2?style=flat-square&logo=googlegemini&logoColor=white" alt="Gemini badge">

#### Test

<img src="https://img.shields.io/badge/Kotest-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotest badge"> <img src="https://img.shields.io/badge/JUnit5-25A162?style=flat-square&logo=junit5&logoColor=white" alt="JUnit 5 badge"> <img src="https://img.shields.io/badge/k6-7D64FF?style=flat-square&logo=k6&logoColor=white" alt="k6 badge">

#### AWS

<img src="https://img.shields.io/badge/AWS_EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white" alt="AWS EC2 badge"> <img src="https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white" alt="AWS S3 badge">

#### CI/CD

<img src="https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=GitHub%20Actions&logoColor=white" alt="GitHub Actions badge"> <img src="https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white" alt="Docker badge">

#### Monitoring

<img src="https://img.shields.io/badge/Discord-5865F2?style=flat-square&logo=discord&logoColor=white" alt="Discord badge"> <img src="https://img.shields.io/badge/Sentry-362D59?style=flat-square&logo=sentry&logoColor=white" alt="Sentry badge">

</br></br>

## 🔨 Architecture
<img width="2048" height="704" alt="image" src="https://github.com/user-attachments/assets/f90be49c-9d2d-4b1d-acb0-e80fe00ec047" />

레이어드 아키텍처 기반의 Gradle 멀티모듈 구조입니다. 자세한 규칙은 [architecture.md](docs/conventions/architecture.md)를 참고하세요.

```
bootstrap → api:{core,owner,admin,user}-api, gateway:{auth,logging},
            infrastructure:{db,redis,client,storage}
api:{owner,admin,user}-api → api:core-api → core
gateway:*, infrastructure:* → core
core → common (core는 그 외 어떤 모듈에도 의존하지 않음)
```

| 모듈 | 역할 |
| --- | --- |
| `bootstrap` | 애플리케이션 실행 진입점, 전체 모듈 조립 및 배포 설정 |
| `api:core-api` | API 공통 인프라 (ApiResponse, GlobalExceptionHandler 등) |
| `api:owner-api` / `api:user-api` / `api:admin-api` | role별 Controller 모듈 (서로 의존하지 않음) |
| `core` | 도메인 객체, Service, Implement Layer, Repository 인터페이스 (순수 Kotlin) |
| `gateway:auth` / `gateway:logging` | 인증(Spring Security, JWT, OAuth2), 요청 로깅 |
| `infrastructure:db` | JPA Entity, Repository 구현체, Flyway 마이그레이션 |
| `infrastructure:redis` | 캐시, 조회수 집계, pub/sub |
| `infrastructure:client` | 외부 연동 (Gemini, Discord, 공휴일 API 등) |
| `infrastructure:storage` | S3 이미지 업로드 |
| `common` | 공통 상수·유틸 |

```
Presentation (Controller) → Business (Service) → Implement (Reader/Appender/...) → Data Access (Repository)
```

### Implement Layer

[지속 성장 가능한 소프트웨어를 만들어가는 방법](https://geminikims.medium.com/%EC%A7%80%EC%86%8D-%EC%84%B1%EC%9E%A5-%EA%B0%80%EB%8A%A5%ED%95%9C-%EC%86%8C%ED%94%84%ED%8A%B8%EC%9B%A8%EC%96%B4%EB%A5%BC-%EB%A7%8C%EB%93%A4%EC%96%B4%EA%B0%80%EB%8A%94-%EB%B0%A9%EB%B2%95-97844c5dab63)을 참고하여 Business와 Data Access 사이에 **Implement Layer**를 두었습니다.

- Service가 Repository를 직접 호출하는 대신, 단일 책임을 갖는 구현 컴포넌트(`Reader`, `Appender`, `Remover`, `Validator` 등)를 조합해 비즈니스 흐름을 표현합니다.
- 세부 구현 로직이 재사용 가능한 단위로 격리되어 Service는 "무엇을 하는지"만 드러내고, 도메인이 커져도 로직 중복 없이 확장할 수 있습니다.
- 역할별 세분화는 강제가 아니라 도메인 특성에 따라 유연하게 적용합니다 — 핵심 도메인은 역할별로 나누고, 비핵심 도메인은 `Manager` 하나로 통합하기도 합니다.

</br></br>

## ▶️ Getting Started

```bash
./gradlew build                  # 전체 빌드 (ktlint 검사 포함)
./gradlew test                   # 전체 테스트
./gradlew :bootstrap:bootRun     # 애플리케이션 실행
```

로컬 실행에 필요한 환경 변수는 `.env.example`을 참고하세요.
