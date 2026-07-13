import http from 'k6/http';
import { check } from 'k6';
import { baseUrl } from '../config/env.js';

/**
 * OWNER access token 확보.
 * - OWNER_ACCESS_TOKEN 이 설정돼 있으면 그대로 쓴다 (스테이징/운영 대상일 때).
 * - 없으면 GET /v1/auth/local-token 으로 발급한다 — local 프로파일 서버에서만 동작한다.
 *
 * 엑셀 분석은 마트 소유권을 검사하므로, 토큰의 userId 는 대상 마트(IMPORT_MARKET_IDS)의
 * 소유자여야 한다. local-token 발급 시 OWNER_USER_ID 로 지정한다.
 */
export function resolveOwnerToken() {
  const preset = __ENV.OWNER_ACCESS_TOKEN;
  if (preset) {
    return preset;
  }

  const userId = __ENV.OWNER_USER_ID || '1';
  const res = http.get(`${baseUrl()}/v1/auth/local-token?role=OWNER&userId=${userId}`);
  check(res, { 'local-token 발급 200': (r) => r.status === 200 });

  const token = res.json('data');
  if (!token) {
    throw new Error(
      'OWNER 토큰 발급 실패. OWNER_ACCESS_TOKEN 을 직접 넣거나, local 프로파일로 뜬 서버를 대상으로 하세요.',
    );
  }
  return token;
}

export function bearer(token) {
  return { Authorization: `Bearer ${token}` };
}
