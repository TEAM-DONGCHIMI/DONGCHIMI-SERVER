import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';
import { resolveProfile } from '../config/options.js';
import { baseUrl, profileName } from '../config/env.js';
import { resolveOwnerToken, bearer } from '../lib/auth.js';
import { marketIdFor, excelFileUrl, importTimeout } from '../lib/import-config.js';

// 동기 파이프라인(POST .../import/sync)은 분석이 끝날 때까지 블로킹하므로 응답 시간 = 처리 시간이다.
// 클라이언트 wall-clock 과, 서버가 응답 body 에 담아주는 단계별 소요시간을 모두 수집한다.
const endToEnd = new Trend('import_sync_end_to_end', true);
const serverElapsed = new Trend('import_sync_server_elapsed', true);
const stageClassify = new Trend('import_sync_stage_classify', true);
const stageMatch = new Trend('import_sync_stage_match', true);

export const options = {
  scenarios: { import_sync: resolveProfile(profileName()) },
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
  const url = `${baseUrl()}/v1/owners/markets/${marketId}/products/import/sync`;
  const payload = JSON.stringify({ excelFileUrl: excelFileUrl() });
  const headers = { ...bearer(data.token), 'Content-Type': 'application/json' };

  const started = Date.now();
  const res = http.post(url, payload, { headers, timeout: importTimeout() });
  endToEnd.add(Date.now() - started);

  const ok = check(res, {
    'sync 200': (r) => r.status === 200,
    'sync success=true': (r) => r.json('success') === true,
  });

  if (ok) {
    const d = res.json('data');
    if (d) {
      serverElapsed.add(d.elapsedMs);
      stageClassify.add(d.stageElapsedMs.classify);
      stageMatch.add(d.stageElapsedMs.match);
    }
  }
}
