/**
 * 엑셀 분석 벤치마크(동기/비동기 공용) 설정. 두 시나리오가 같은 마트 풀·엑셀 파일을 쓰도록 한곳에 모은다.
 */

/**
 * VU 를 마트 풀에 라운드로빈으로 배정한다. 동기·비동기 모두 분석 결과를 대상 마트의
 * prepared_products 에 통째로 덮어쓰므로(soft delete + insert), 여러 VU 가 같은 마트를
 * 동시에 치면 서로의 결과를 지운다. VU 마다 다른 마트를 주면 이 경합을 피한다.
 */
export function marketIdFor(vu) {
  const raw = __ENV.IMPORT_MARKET_IDS;
  if (!raw) {
    throw new Error('IMPORT_MARKET_IDS 환경변수가 필요합니다 (예: "1" 또는 "1,2,3").');
  }
  const ids = raw.split(',').map((s) => s.trim()).filter(Boolean);
  if (ids.length === 0) {
    throw new Error('IMPORT_MARKET_IDS 가 비어 있습니다.');
  }
  return ids[(vu - 1) % ids.length];
}

/** S3 에 미리 올려둔 분석용 엑셀 파일 URL. 다운로드(읽기)만 하므로 모든 VU 가 공유해도 된다. */
export function excelFileUrl() {
  const url = __ENV.IMPORT_EXCEL_FILE_URL;
  if (!url) {
    throw new Error('IMPORT_EXCEL_FILE_URL 환경변수가 필요합니다 (S3에 업로드된 엑셀 URL).');
  }
  return url;
}

/** 한 번의 분석이 오래 걸릴 수 있어 기본 http timeout(60s)보다 넉넉히 잡는다. */
export function importTimeout() {
  return __ENV.IMPORT_TIMEOUT || '120s';
}
