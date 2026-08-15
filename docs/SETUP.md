# Setup

이 문서는 CommerceFlow를 로컬에서 실행하고 검증하기 위한 기본 설정을 설명한다.

## Gradle Wrapper

Gradle Wrapper는 프로젝트에 포함되는 Gradle 실행 도구다.

추가된 파일은 다음과 같다.

```text
gradlew
gradlew.bat
gradle/wrapper/gradle-wrapper.jar
gradle/wrapper/gradle-wrapper.properties
```

로컬 PC에 Gradle이 설치되어 있지 않아도 아래 명령으로 빌드와 테스트를 실행할 수 있다.

```powershell
.\gradlew.bat test
```

macOS 또는 Linux 환경에서는 다음 명령을 사용한다.

```bash
./gradlew test
```

`gradle-wrapper.properties`에는 프로젝트가 사용할 Gradle 배포 버전이 기록된다. 이 프로젝트는 Gradle `8.10.2`를 사용한다.

## Java Version

명세 기준 Java 버전은 21이다. 현재 개발 PC에는 Java 22가 설치되어 있으므로, Gradle 설정에서 Java 21 호환 바이트코드가 생성되도록 `options.release = 21`을 사용한다.

이 설정은 Java 22 런타임에서도 Java 21 대상 컴파일 결과를 만들기 위한 것이다. 운영 또는 CI 환경에서는 Java 21 사용을 우선한다.

## Docker Compose

Phase 0에서는 애플리케이션 실행에 필요한 최소 인프라만 Docker Compose에 포함한다.

```text
postgresql
redis
```

실행 명령은 다음과 같다.

```bash
docker compose up -d
```

Windows에서는 Docker Desktop 엔진이 실행 중이어야 한다.

Compose 문법 검증은 다음 명령으로 수행한다.

```bash
docker compose config
```

Kafka, Prometheus, Grafana는 명세에 포함되어 있지만 Phase 0에서는 추가하지 않는다. 각 기능이 필요한 단계에서 설정과 검증을 함께 추가한다.

## Spring Profiles

설정 파일은 실행 환경별로 분리한다.

```text
application.yml
application-local.yml
application-test.yml
```

`application.yml`은 공통 설정을 담고, 기본 활성 프로필은 `local`이다.

`application-local.yml`은 로컬 PostgreSQL과 Redis 연결 설정을 담는다.

`application-test.yml`은 테스트 실행을 위한 설정이다. 현재는 빠른 context 테스트를 위해 H2를 사용하고 Flyway를 비활성화한다. 운영과 유사한 통합 테스트가 필요한 단계에서는 Testcontainers PostgreSQL을 별도로 사용한다.

## Environment Variables

운영 환경에서는 secret을 설정 파일에 직접 쓰지 않고 환경변수로 주입한다.

현재 필요한 환경변수는 다음과 같다.

```text
JWT_SECRET
```

local 프로필은 개발 편의를 위해 기본값을 제공한다. dev/prod 프로필을 추가할 때는 기본값 없는 환경변수 참조를 사용해 secret 누락 시 애플리케이션이 실패하도록 구성한다.

## Health Check and Swagger

기본 Health Check API는 다음 경로로 제공한다.

```text
GET /api/v1/health
```

예상 응답은 공통 응답 형식을 따른다.

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

Swagger UI는 애플리케이션 실행 후 브라우저에서 접속한다.

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON 문서는 다음 경로에서 확인할 수 있다.

```text
http://localhost:8080/v3/api-docs
```

테스트에서는 브라우저를 직접 띄우지 않고 `/api/v1/health`, `/v3/api-docs`, `/swagger-ui/index.html` 엔드포인트를 MockMvc로 확인한다. 이렇게 하면 Swagger 설정이 깨졌는지 자동 테스트에서 잡을 수 있다.

## Verification

현재 검증된 항목은 다음과 같다.

```text
docker compose config
docker compose up -d
docker compose ps
docker exec commerceflow-postgresql pg_isready -U commerceflow -d commerceflow
docker exec commerceflow-redis redis-cli ping
.\gradlew.bat test
.\gradlew.bat bootRun --args="--spring.main.web-application-type=none"
```

검증 결과는 다음과 같다.

```text
PostgreSQL container: healthy
PostgreSQL pg_isready: accepting connections
Redis container: healthy
Redis ping: PONG
Spring Boot local profile startup: success
Gradle test: success
Health Check API smoke test: success
OpenAPI docs smoke test: success
Swagger UI smoke test: success
Login API test: success
JWT protected API test: success
Refresh Token reissue test: success
Logout refresh token deletion test: success
Local HTTP signup/login/reissue with Redis storage: success
```

Refresh Token Redis 저장은 로컬 애플리케이션을 임시 실행한 뒤 실제 HTTP 요청으로도 확인했다.

```text
POST /api/v1/members/signup
POST /api/v1/auth/login
Redis key auth:refresh-token:{memberId} created
POST /api/v1/auth/reissue
```

검증용 회원과 Redis key는 확인 후 삭제했다.

`bootRun` 검증은 web server를 계속 띄우지 않기 위해 `--spring.main.web-application-type=none` 옵션을 사용한다. 이 방식은 PostgreSQL, Flyway, JPA 초기화가 정상인지 빠르게 확인하기 위한 것이다.
