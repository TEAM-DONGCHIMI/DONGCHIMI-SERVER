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
