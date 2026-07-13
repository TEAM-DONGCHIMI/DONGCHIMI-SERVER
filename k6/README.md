# k6 성능 테스트

k6 기반 성능(부하) 테스트 환경. 아직 구체적인 API 시나리오는 없고, 시나리오를 추가할 때 재사용할 공용 골격만 있는 상태다.

## 디렉터리 구조

```
k6/
├── docker-compose.perf.yml   # k6 실행기
├── .env.perf.example         # k6 컨테이너 env 템플릿 (복사해 .env.perf로 사용)
├── config/
│   ├── options.js            # 부하 프로파일(smoke/load/stress/spike)·thresholds
│   └── env.js                # BASE_URL 등 env 파싱
├── lib/                      # 시나리오 공용 헬퍼 (아직 없음, 필요할 때 추가)
├── scenarios/                # API별 테스트 스크립트 (아직 없음)
└── results/                  # handleSummary 산출물 (git 미추적)
```

## 새 시나리오 추가하는 법

1. `scenarios/<domain>-<action>.js` 작성. `config/options.js`의 `PROFILES`/`DEFAULT_THRESHOLDS`, `config/env.js`의 `baseUrl()`/`profileName()`을 재사용한다.
2. 해당 시나리오에서만 쓰는 헬퍼(HTTP 호출, check, 커스텀 메트릭 등)는 `lib/`에 둔다.
3. 외부 의존(카카오처럼 서드파티 API를 호출하는 기능)이 있다면, 그 부분을 어떻게 처리할지(실제 호출 vs 별도 프로파일로 코드 대체 등) 먼저 정하고 시작한다 — 이전에 카카오 로그인 시나리오에서 인프라 부담과 abuse 이슈 때문에 여러 번 방향을 바꾼 적이 있다.
4. DB에 사전 데이터가 필요하면 `data/` 디렉터리를 만들어 시딩/정리 SQL을 둔다.

## 실행

```bash
cp k6/.env.perf.example k6/.env.perf
# BASE_URL, PROFILE 수정

docker compose -f k6/docker-compose.perf.yml run --rm k6 run scenarios/<파일명>.js
```

## 결과 확인

- 콘솔에 k6 요약이 출력된다.
- `results/`에 `handleSummary`로 JSON 등을 저장하도록 시나리오에서 구현하면 된다 (예시는 아직 없음).
- 또는 실행 시 `--summary-export=results/<이름>.json` 플래그로 요약을 파일로 뽑을 수 있다.

## 상품 등록 엑셀 분석 벤치마크 (동기 vs 비동기)

같은 엑셀 파일을 **동기 파이프라인**(`POST .../import/sync`, 요청 스레드에서 블로킹)과
**비동기 파이프라인**(`POST .../import` → SSE progress)으로 각각 돌려 처리 시간을 비교한다.

- `scenarios/product-import-sync.js` — 단일 POST 가 완료까지 블로킹. 응답 body 의 `elapsedMs`·
  `stageElapsedMs`(단계별)까지 커스텀 메트릭으로 수집한다.
- `scenarios/product-import-async.js` — jobId 를 받고, progress SSE 가 `completed` 이벤트에서
  닫힐 때까지 GET 이 블로킹되는 성질을 이용해 end-to-end wall-clock 을 잰다(k6 코어는 SSE 미지원).

### 사전 준비

1. **버릴 수 있는 테스트 마트**를 준비한다. 분석은 그 마트의 `prepared_products` 를 통째로 덮어쓴다.
2. 분석용 엑셀을 S3(우리 버킷)에 올리고 그 URL 을 `IMPORT_EXCEL_FILE_URL` 에 넣는다.
3. `.env.perf` 의 `IMPORT_MARKET_IDS`, 인증(`OWNER_ACCESS_TOKEN` 또는 `OWNER_USER_ID`)을 채운다.
   토큰의 userId 는 대상 마트의 소유자여야 한다.
4. 동기·비동기의 AI provider(`import.ai.provider`: mock/gemini)를 **양쪽 동일**하게 두고 서버를 띄운다.

### 실행

```bash
# 순차 지연(latency) 비교 — 1 VU 로 표본 IMPORT_ITERATIONS 회
docker compose -f k6/docker-compose.perf.yml run --rm \
  -e PROFILE=importLatency k6 run scenarios/product-import-sync.js  --summary-export=results/import-sync.json
docker compose -f k6/docker-compose.perf.yml run --rm \
  -e PROFILE=importLatency k6 run scenarios/product-import-async.js --summary-export=results/import-async.json

# 동시 처리량(throughput) 비교 — IMPORT_VUS 개 VU (마트 풀도 그만큼 넣을 것)
docker compose -f k6/docker-compose.perf.yml run --rm \
  -e PROFILE=importThroughput -e IMPORT_VUS=5 k6 run scenarios/product-import-sync.js
```

### 비교 지표

- `import_sync_end_to_end` vs `import_async_end_to_end` — 클라이언트 wall-clock (핵심 비교값).
- `import_sync_stage_classify` / `import_sync_stage_match` — 동기에서 AI 단계가 차지하는 시간
  (동시성 제거로 비동기 대비 얼마나 늘어나는지).
- `import_async_start_latency` — 비동기가 jobId 를 돌려주기까지의 시간(즉시 응답 이점).

> ⚠ 동기 시나리오는 요청 하나가 Tomcat 워커 스레드를 수 초~수십 초 점유한다. `IMPORT_VUS` 를
> `server.tomcat.threads.max` 안에서 잡고, 마트 풀 크기(`IMPORT_MARKET_IDS`)를 VU 수 이상으로 둔다.
