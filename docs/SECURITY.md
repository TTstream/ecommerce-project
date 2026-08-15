# Security

이 문서는 CommerceFlow의 보안 설계와 운영 주의사항을 기록한다.

## Current Scope

현재 구현된 보안 범위는 다음과 같다.

```text
BCrypt password encoding
Login API
JWT Access Token
Bearer token authentication
Common JSON response for authentication and authorization failures
```

아직 구현하지 않은 범위는 다음과 같다.

```text
Refresh Token
Refresh Token rotation
Logout
Redis token storage
Admin role authorization
Rate limit
Brute force protection
```

## Password

회원가입 시 비밀번호는 BCrypt로 암호화한 뒤 저장한다.

원문 비밀번호는 다음 위치에 남기지 않는다.

```text
API response
log
database
```

## JWT

Access Token은 로그인 성공 시 발급한다.

현재 기본 만료시간은 30분이다.

```yaml
app:
  jwt:
    access-token-validity: 30m
```

JWT secret은 운영 환경에서 환경변수로 주입해야 한다.

```text
JWT_SECRET
```

local/test 프로필의 secret은 개발과 테스트 실행을 위한 값이다. 운영 secret을 Git에 커밋하지 않는다.

## Authentication Flow

```text
Client
-> POST /api/v1/auth/login
-> Access Token 발급
-> Authorization: Bearer {accessToken}
-> JwtAuthenticationFilter
-> SecurityContext 저장
-> Controller @AuthenticationPrincipal 사용
```

## Failure Response

인증이 없거나 토큰이 유효하지 않으면 `UNAUTHORIZED`를 반환한다.

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다.",
    "fieldErrors": []
  },
  "timestamp": "2026-01-01T00:00:00"
}
```

권한이 부족하면 `FORBIDDEN`을 반환한다.

로그인 실패는 `INVALID_CREDENTIALS`를 반환한다. 이메일 존재 여부를 외부에 노출하지 않기 위해 존재하지 않는 이메일과 틀린 비밀번호를 같은 응답으로 처리한다.

## Operational Notes

운영 배포 시 확인할 항목은 다음과 같다.

```text
JWT_SECRET is set through environment variables
JWT_SECRET is at least 32 characters
Access token validity is appropriate for the service risk level
Sensitive values are not logged
HTTPS is enforced at the edge or application gateway
```
