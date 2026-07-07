export function baseUrl() {
  const value = __ENV.BASE_URL;
  if (!value) {
    throw new Error('BASE_URL 환경변수가 필요합니다 (예: BASE_URL=https://staging.dongchimi.example.com)');
  }
  return value.replace(/\/$/, '');
}

export function profileName() {
  return __ENV.PROFILE || 'smoke';
}
