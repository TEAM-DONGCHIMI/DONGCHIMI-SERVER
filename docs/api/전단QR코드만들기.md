# 전단 QR코드 만들기

Actor: 점주
HTTP Method: POST
도메인: 마트 (Market)
서버 담당자: 김채현
엔드포인트: /v1/owners/markets/{marketId}/flyers/qr
인증 여부: 인증 필요
진행 상태: API 개발 중
클라 담당자: 이채원

<aside>
<img src="https://app.notion.com/icons/description_gray.svg" alt="https://app.notion.com/icons/description_gray.svg" width="40px" />

마트의 전단 QR 코드를 발행하는 API
(slug, QR코드 생성)

</aside>

## 사용 뷰

![App.png](%EC%A0%84%EB%8B%A8%20QR%EC%BD%94%EB%93%9C%20%EB%A7%8C%EB%93%A4%EA%B8%B0/App.png)

![image.png](%EC%A0%84%EB%8B%A8%20QR%EC%BD%94%EB%93%9C%20%EB%A7%8C%EB%93%A4%EA%B8%B0/image.png)

## Request

### Request Parameter

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `marketId` | Number | O | 마트 id |

## Response

### 200: SUCCESS

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "요청에 성공했습니다.",
  "data": {
    "qrCode": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
  }
}
```

#### data 필드 설명

| Field | Type | Required | Description |
| --- | --- | --- | --- |
| `qrCode` | String | O | QR코드 이미지 (Base64 인코딩) |

### 401: UNAUTHORIZED

```json
{
  "success": false,
  "code": "UNAUTHORIZED",
  "message": "인증이 필요합니다."
}
```

### 403: FORBIDDEN_MARKET_ACCESS

```json
{
  "success": false,
  "code": "FORBIDDEN_MARKET_ACCESS",
  "message": "해당 마트에 대한 접근 권한이 없습니다."
}
```

### 404: MARKET_NOT_FOUND

```json
{
  "success": false,
  "code": "MARKET_NOT_FOUND",
  "message": "존재하지 않는 마트입니다."
}
```