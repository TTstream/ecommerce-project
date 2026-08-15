# Decisions

이 문서는 프로젝트 진행 중 발생한 주요 기술 선택과 이유를 기록한다.

## 1. Single Module First

초기 구조는 단일 모듈 Spring Boot 프로젝트로 시작한다.

이유는 다음과 같다.

- 현재 단계에서는 도메인 경계보다 실행 가능한 기반 구축이 우선이다.
- 멀티모듈은 빌드 설정과 의존성 관리 복잡도를 먼저 만든다.
- 명세의 도메인들은 아직 코드로 충분히 드러나지 않았으므로, 실제 변경 이유 없이 모듈을 나누면 과한 추상화가 된다.
- 포트폴리오 관점에서는 단일 모듈 안에서 도메인 중심 패키지를 명확히 잡고, 필요해지는 시점에 모듈 분리 근거를 문서화하는 편이 더 설득력 있다.

현재 패키지 구조는 명세의 도메인 중심 구조를 따른다.

```text
com.portfolio.commerceflow
├── common
├── member
├── product
├── cart
├── order
├── payment
├── coupon
├── inventory
├── notification
├── batch
└── admin
```

## 2. Gradle Wrapper

Gradle Wrapper를 추가했다.

이유는 다음과 같다.

- 개발자 PC에 Gradle이 없어도 빌드할 수 있다.
- CI 환경에서도 같은 Gradle 버전을 사용한다.
- 프로젝트를 clone한 사람이 별도 Gradle 설치 없이 테스트를 실행할 수 있다.
- 빌드 실패 원인을 로컬 Gradle 버전 차이에서 분리할 수 있다.

현재 Wrapper 버전은 Gradle `8.10.2`다.

## 3. .gitattributes

`.gitattributes`를 추가했다.

이 파일은 Git이 파일별 줄바꿈을 어떻게 관리할지 정의한다.

Windows는 보통 `CRLF`, macOS/Linux는 `LF`를 사용한다. 이 차이 때문에 불필요한 diff가 생기거나, Linux에서 실행되는 `gradlew` 스크립트가 깨질 수 있다.

현재 정책은 다음과 같다.

```text
Java, Gradle, YAML, Markdown, SQL, properties, gradlew: LF
Windows batch 파일: CRLF
```

목적은 운영체제 차이로 인한 변경 노이즈를 줄이고, CI나 Linux 환경에서 wrapper 스크립트가 안정적으로 실행되도록 하는 것이다.

## 4. Java 21 Target With Local Java 22

명세는 Java 21을 요구한다.

현재 로컬 PC에는 Java 22가 설치되어 있었다. Gradle toolchain을 Java 21로 강제하면 로컬에 Java 21이 없을 때 빌드가 실패한다.

따라서 현재는 다음 방식을 선택했다.

```gradle
java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType(JavaCompile).configureEach {
    options.release = 21
}
```

이 설정은 Java 22 컴파일러를 사용하더라도 Java 21 API와 바이트코드 수준을 대상으로 컴파일한다.

향후 CI를 구성할 때는 Java 21을 명시적으로 설치하고, 필요하면 Gradle toolchain 설정으로 되돌릴 수 있다.

## 5. Minimal Infrastructure in Phase 0

Phase 0의 Docker Compose에는 PostgreSQL과 Redis만 포함했다.

Kafka, Prometheus, Grafana도 최종 명세에는 포함되어 있지만, 아직 해당 기능을 사용하는 코드가 없다.

초기부터 모든 인프라를 올리면 실행 실패 원인이 많아지고 Phase 0 검증 범위가 흐려진다. 따라서 각 기술은 실제 사용 단계에서 설정, 코드, 테스트, 문서를 함께 추가한다.

## 6. Member Signup as the First Phase 1 Slice

Phase 1은 회원가입, 로그인, JWT, Refresh Token, 권한 처리를 포함한다.

첫 구현 범위는 회원가입으로 제한했다.

이유는 다음과 같다.

- 회원가입은 인증 기능의 기반 데이터인 Member를 먼저 만든다.
- 로그인과 JWT는 Member 저장 구조와 비밀번호 암호화가 안정된 뒤 붙이는 편이 테스트 범위가 명확하다.
- 한 번에 Refresh Token과 Redis까지 붙이면 실패 원인이 DB, Security, JWT, Redis로 넓어진다.
- 명세의 원칙처럼 기능 단위로 설계, 구현, 테스트, 문서화를 끝내기 위함이다.

이번 단계에서는 Spring Security를 추가하되 인증 필터나 JWT는 만들지 않는다. Spring Security는 BCrypt `PasswordEncoder`를 사용하고, 이후 인증/인가 설정을 자연스럽게 확장하기 위한 기반으로만 사용한다.

Spring Security가 기본 개발용 사용자를 자동 생성하지 않도록 임시 `UserDetailsService` bean을 등록했다. 실제 로그인 단계에서는 이 bean을 회원 조회 기반 구현으로 교체한다.

Spring Data Redis Repository 스캔은 비활성화했다. 이 프로젝트에서 Redis는 Refresh Token, 캐시, 인기 상품, 분산 락 용도로 사용할 예정이며, JPA Entity를 Redis Repository 후보로 스캔할 필요가 없기 때문이다.

회원가입 트랜잭션은 application service에 둔다.

```text
MemberController
-> MemberSignupService @Transactional
-> MemberRepository
```

Controller는 요청 검증과 응답 변환만 담당하고, 이메일 중복 확인과 비밀번호 암호화, 저장은 service에서 처리한다.

이메일 중복은 application service에서 먼저 검사하고, DB unique constraint로 최종 방어한다. 동시 요청에서는 두 요청이 application level 검사를 모두 통과할 수 있으므로, DB 제약 조건이 마지막 정합성 보장선이다.

## 7. JWT Login Before Refresh Token

Phase 1-2에서는 로그인과 Access Token 발급까지만 구현했다.

Refresh Token, Redis 저장, 로그아웃, 토큰 재발급은 다음 단계로 분리한다.

이유는 다음과 같다.

- Access Token 인증 흐름이 안정되어야 Refresh Token 회전 전략을 붙일 수 있다.
- Redis 저장까지 한 번에 구현하면 인증 실패 원인이 JWT, DB, Redis로 넓어진다.
- 로그인 성공, 로그인 실패, 보호 API 접근 제어를 먼저 테스트로 고정하는 편이 변경 범위를 명확히 한다.

JWT 설정은 `app.jwt` prefix로 분리했다.

```yaml
app:
  jwt:
    issuer: commerceflow
    access-token-validity: 30m
    secret: ${JWT_SECRET}
```

`issuer`와 만료시간은 공통 설정으로 둘 수 있지만, secret은 운영 환경에서 반드시 환경변수로 주입한다. local/test 프로필에는 개발과 테스트 실행을 위한 값만 둔다.

JWT 인증은 `OncePerRequestFilter` 기반으로 처리한다.

```text
Authorization: Bearer {accessToken}
-> JwtAuthenticationFilter
-> JwtTokenProvider
-> SecurityContext
-> @AuthenticationPrincipal
```

인증 실패와 인가 실패 응답은 Spring Security 기본 HTML/빈 응답을 사용하지 않고 공통 JSON 응답으로 맞췄다.

```text
401 UNAUTHORIZED
403 FORBIDDEN
```

로그인 실패 시에는 이메일 존재 여부를 노출하지 않기 위해 존재하지 않는 이메일과 틀린 비밀번호 모두 `INVALID_CREDENTIALS`를 반환한다.
