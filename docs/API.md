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

## Login

```http
POST /api/v1/auth/login
```

이메일과 비밀번호로 로그인하고 Access Token을 발급한다.

### Request

```json
{
  "email": "user@example.com",
  "password": "password1"
}
```

### Response: 200 OK

```json
{
  "success": true,
  "data": {
    "tokenType": "Bearer",
    "accessToken": "eyJ...",
    "expiresIn": 1800
  },
  "error": null,
  "timestamp": "2026-01-01T00:00:00"
}
```

`expiresIn` 단위는 초다.

### Error: 401 Unauthorized

이메일이 존재하지 않거나 비밀번호가 틀리면 `INVALID_CREDENTIALS`를 반환한다.

계정 존재 여부를 외부에 노출하지 않기 위해 두 상황 모두 같은 에러 코드를 사용한다.

## My Profile

```http
GET /api/v1/members/me
Authorization: Bearer {accessToken}
```

인증된 회원의 프로필을 조회한다.

### Response: 200 OK

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "name": "tester",
    "role": "USER",
    "status": "ACTIVE"
  },
  "error": null,
  "timestamp": "2026-01-01T00:00:00"
}
```

### Error: 401 Unauthorized

Access Token이 없거나 유효하지 않으면 `UNAUTHORIZED`를 반환한다.

## Swagger

Swagger UI는 다음 주소에서 확인한다.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON은 다음 주소에서 확인한다.

```text
http://localhost:8080/v3/api-docs
```
