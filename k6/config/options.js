// 공용 부하 프로파일. scenarios/*.js 에서 PROFILE env로 선택해 재사용한다.
export const PROFILES = {
  smoke: {
    executor: 'constant-vus',
    vus: 1,
    duration: '30s',
  },
  load: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '1m', target: 20 },
      { duration: '3m', target: 20 },
      { duration: '1m', target: 0 },
    ],
  },
  stress: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '2m', target: 20 },
      { duration: '2m', target: 50 },
      { duration: '2m', target: 100 },
      { duration: '2m', target: 150 },
      { duration: '2m', target: 0 },
    ],
  },
  spike: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '10s', target: 5 },
      { duration: '30s', target: 200 },
      { duration: '10s', target: 5 },
      { duration: '30s', target: 0 },
    ],
  },
};

export function resolveProfile(name) {
  const profile = PROFILES[name];
  if (!profile) {
    throw new Error(`알 수 없는 프로파일: ${name} (사용 가능: ${Object.keys(PROFILES).join(', ')})`);
  }
  return profile;
}

// 초기값. baseline 측정 후 실측치 기준으로 조정한다 (docs/plans/k6-performance-testing-plan.md 3-4).
export const DEFAULT_THRESHOLDS = {
  http_req_failed: ['rate<0.01'],
  http_req_duration: ['p(95)<300', 'p(99)<800'],
  checks: ['rate>0.99'],
};
