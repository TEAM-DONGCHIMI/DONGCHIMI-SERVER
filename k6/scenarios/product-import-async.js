import http from 'k6/http';
import { check, fail } from 'k6';
import { Trend } from 'k6/metrics';
import { resolveProfile } from '../config/options.js';
import { baseUrl, profileName } from '../config/env.js';
import { resolveOwnerToken, bearer } from '../lib/auth.js';
import { marketIdFor, excelFileUrl, importTimeout } from '../lib/import-config.js';

// 비동기 파이프라인의 end-to-end wall-clock:
//   POST .../import (즉시 jobId 반환) → GET .../{jobId}/progress (SSE) 로 completed 까지 대기.
// k6 코어는 SSE 를 모르지만, 이 progress 스트림은 completed/failed/canceled 종료 이벤트를 내보내면
// 서버가 emitter 를 complete() 해 커넥션을 닫는다. 따라서 일반 GET 이 그 시점까지 블로킹됐다가
// 누적된 전체 SSE 바디를 반환한다 — 이 GET 의 소요시간이 곧 "분석이 끝날 때까지 걸린 시간"이다.
const endToEnd = new Trend('import_async_end_to_end', true);
const startLatency = new Trend('import_async_start_latency', true);

export const options = {
  scenarios: { import_async: resolveProfile(profileName()) },
  thresholds: {
    checks: ['rate>0.99'],
    http_req_failed: ['rate<0.01'],
  },
};

export function setup() {
  return { token: resolveOwnerToken() };
}

export default function (data) {
  const marketId = marketIdFor(__VU);
  const token = data.token;
  const started = Date.now();

  // 1) 분석 시작 → jobId 즉시 반환
  const startRes = http.post(
    `${baseUrl()}/v1/owners/markets/${marketId}/products/import`,
    JSON.stringify({ excelFileUrl: excelFileUrl() }),
    { headers: { ...bearer(token), 'Content-Type': 'application/json' }, timeout: importTimeout() },
  );
  startLatency.add(Date.now() - started);

  const startedOk = check(startRes, {
    'async start 200': (r) => r.status === 200,
    'jobId 반환': (r) => !!r.json('data.jobId'),
  });
  if (!startedOk) {
    fail('분석 시작 실패로 이번 iteration 중단');
  }

  const jobId = startRes.json('data.jobId');

  // 2) SSE progress — 종료 이벤트에서 스트림이 닫힐 때까지 블로킹
  const progressRes = http.get(
    `${baseUrl()}/v1/owners/markets/${marketId}/products/import/${jobId}/progress`,
    { headers: { ...bearer(token), Accept: 'text/event-stream' }, timeout: importTimeout() },
  );
  endToEnd.add(Date.now() - started);

  check(progressRes, {
    'progress 200': (r) => r.status === 200,
    'completed 이벤트 수신': (r) => /event:\s*completed/.test(r.body || ''),
  });
}
