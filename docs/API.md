# API

이 문서는 CommerceFlow API의 현재 구현 상태를 기록한다.

## Common Response

모든 API 응답은 공통 응답 형식을 사용한다.

### Success

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-01-01T00:00:00"
}
```

### Failure

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "INVALID_INPUT_VALUE",
    "message": "입력값이 올바르지 않습니다.",
    "fieldErrors": []
  },
  "timestamp": "2026-01-01T00:00:00"
}
```

## Health Check

```http
GET /api/v1/health
```

서버가 기본 요청을 처리할 수 있는지 확인한다.

### Response

```json
{
  "success": true,
  "data": {
    "status": "UP"
  },
  "error": null,
  "timestamp": "2026-01-01T00:00:00"
}
```

## Member Signup

```http
POST /api/v1/members/signup
```

신규 회원을 생성한다.

### Request

```json
{
  "email": "user@example.com",
  "password": "password1",
  "name": "tester"
}
```

### Validation

```text
email: required, email format, max 255
password: required, 8-64 chars, must include letter and digit
name: required, max 50
```

### Response: 201 Created

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "tester",
    "role": "USER"
  },
  "error": null,
  "timestamp": "2026-01-01T00:00:00"
}
```

비밀번호는 응답에 포함하지 않는다.

### Error: 400 Bad Request

입력값 검증에 실패하면 `INVALID_INPUT_VALUE`를 반환한다.

### Error: 409 Conflict

이미 사용 중인 이메일이면 `DUPLICATED_EMAIL`을 반환한다.

## Swagger

Swagger UI는 다음 주소에서 확인한다.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON은 다음 주소에서 확인한다.

```text
http://localhost:8080/v3/api-docs
```
