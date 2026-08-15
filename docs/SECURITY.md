# Security

이 문서는 CommerceFlow의 보안 설계와 운영 주의사항을 기록한다.

## Current Scope

현재 구현된 보안 범위는 다음과 같다.

```text
BCrypt password encoding
Login API
JWT Access Token
JWT Refresh Token
Redis Refresh Token storage
Refresh Token rotation
Logout
Bearer token authentication
Common JSON response for authentication and authorization failures
```

아직 구현하지 않은 범위는 다음과 같다.

```text
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
    refresh-token-validity: 14d
```

JWT secret은 운영 환경에서 환경변수로 주입해야 한다.

```text
JWT_SECRET
```

local/test 프로필의 secret은 개발과 테스트 실행을 위한 값이다. 운영 secret을 Git에 커밋하지 않는다.

## Refresh Token

Refresh Token은 Redis에 저장한다.

```text
auth:refresh-token:{memberId}
```

Redis value에는 현재 유효한 Refresh Token을 저장하고, TTL은 JWT refresh token 만료시간과 동일하게 둔다.

재발급 API는 요청 Refresh Token이 다음 조건을 모두 만족할 때만 새 토큰을 발급한다.

```text
JWT 서명이 유효함
tokenType이 REFRESH임
만료되지 않음
Redis에 저장된 값과 일치함
회원이 ACTIVE 상태임
```

재발급 성공 시 Refresh Token도 새로 발급해 Redis 값을 교체한다. 이전 Refresh Token은 즉시 무효화된다.

로그아웃 시에는 인증된 회원의 Redis Refresh Token을 삭제한다.

## Authentication Flow

```text
Client
-> POST /api/v1/auth/login
-> Access Token과 Refresh Token 발급
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

Refresh Token 재발급 실패는 `INVALID_REFRESH_TOKEN`을 반환한다.

## Operational Notes

운영 배포 시 확인할 항목은 다음과 같다.

```text
JWT_SECRET is set through environment variables
JWT_SECRET is at least 32 characters
Access token validity is appropriate for the service risk level
Refresh token TTL matches JWT refresh token validity
Sensitive values are not logged
HTTPS is enforced at the edge or application gateway
```
