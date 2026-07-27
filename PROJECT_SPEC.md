# E-Commerce Backend Portfolio Project Specification

> 이 문서는 VS Code의 Codex가 프로젝트의 목표, 기술 구조, 구현 순서, 품질 기준을 이해하고 단계적으로 개발하도록 하기 위한 최상위 개발 명세서다.  
> Codex는 구현 전 반드시 이 문서를 읽고, 현재 단계와 변경 범위를 먼저 설명한 뒤 작업한다.

---

## 1. 프로젝트 개요

### 1.1 프로젝트명

**CommerceFlow**

### 1.2 프로젝트 목적

Java/Spring 기반 백엔드 경력 개발자가 서비스 회사 및 중견 IT 기업에 지원할 때 활용할 수 있는 실무형 포트폴리오를 만든다.

단순 CRUD 구현이 아니라 다음 역량을 보여주는 것을 목표로 한다.

- Spring Boot 기반 REST API 설계
- RDBMS 데이터 모델링 및 SQL 최적화
- JPA와 QueryDSL 활용
- Redis 캐시 및 분산 락
- Kafka 기반 비동기 이벤트 처리
- Spring Batch 기반 정기 작업
- Docker 기반 로컬 실행 환경
- 테스트 자동화
- 운영 관점의 로깅, 모니터링, 예외 처리
- GitHub Actions 기반 CI/CD
- AWS 배포
- 성능 개선 전후 비교
- 장애 및 데이터 정합성 고려

### 1.3 핵심 포트폴리오 메시지

이 프로젝트는 다음 질문에 답할 수 있어야 한다.

1. 왜 이 구조를 선택했는가?
2. 트랜잭션 경계는 어디인가?
3. 동시 주문 시 재고 정합성을 어떻게 보장하는가?
4. Redis 장애 시 서비스는 어떻게 동작하는가?
5. Kafka 중복 소비와 재처리를 어떻게 다루는가?
6. 조회 성능을 어떻게 측정하고 개선했는가?
7. 운영 중 장애를 어떻게 탐지할 수 있는가?
8. 테스트가 무엇을 보장하는가?
9. 배포와 롤백은 어떻게 수행하는가?
10. 기존 구조를 어떻게 리팩터링했는가?

---

## 2. 기술 스택

### 2.1 Backend

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- QueryDSL
- Spring Validation
- Spring Security
- JWT
- Spring Batch
- Spring for Apache Kafka
- Redisson 또는 Spring Data Redis
- Flyway
- Lombok
- MapStruct 선택 사용

### 2.2 Database

- PostgreSQL
- Redis
- H2는 단위 테스트 또는 일부 통합 테스트에만 사용
- 운영과 유사한 테스트는 Testcontainers PostgreSQL 사용

### 2.3 Infrastructure

- Docker
- Docker Compose
- Nginx
- AWS EC2
- AWS RDS PostgreSQL
- AWS ElastiCache는 선택사항
- AWS S3는 상품 이미지 저장 시 선택 사용

### 2.4 Test

- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers
- REST Assured 또는 MockMvc

### 2.5 Documentation and Monitoring

- OpenAPI / Swagger
- Spring Boot Actuator
- Prometheus
- Grafana
- Logback
- JSON 구조 로그는 선택사항

### 2.6 CI/CD

- GitHub Actions
- 자동 빌드
- 자동 테스트
- Docker 이미지 빌드
- EC2 배포

---

## 3. 개발 원칙

### 3.1 기본 원칙

- 한 번에 전체 기능을 구현하지 않는다.
- 기능 단위로 설계, 구현, 테스트, 문서화를 완료한다.
- 비즈니스 로직은 Controller에 작성하지 않는다.
- Entity를 API 응답으로 직접 노출하지 않는다.
- 모든 외부 입력은 검증한다.
- 예외 응답 형식을 통일한다.
- 트랜잭션 범위를 명확하게 관리한다.
- 불필요한 양방향 연관관계를 피한다.
- N+1 문제를 방지하고 테스트 또는 로그로 확인한다.
- 코드 변경 시 관련 테스트도 함께 작성한다.
- 성능 개선은 추측이 아니라 측정 결과를 기반으로 한다.

### 3.2 패키지 구조

기본적으로 도메인 중심 패키지 구조를 사용한다.

```text
src/main/java/com/portfolio/commerceflow
├── common
│   ├── config
│   ├── exception
│   ├── response
│   ├── security
│   └── util
├── member
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── product
│   ├── api
│   ├── application
│   ├── domain
│   └── infrastructure
├── cart
├── order
├── payment
├── coupon
├── inventory
├── notification
├── batch
└── admin
```

필요 이상으로 추상화하지 않는다. 실제 변경 이유가 있을 때 인터페이스를 분리한다.

---

## 4. 시스템 기능 범위

## 4.1 회원

### 필수 기능

- 회원가입
- 로그인
- 로그아웃
- Access Token 발급
- Refresh Token 발급 및 재발급
- 비밀번호 암호화
- 회원 정보 조회
- 회원 정보 수정
- 권한 구분
  - USER
  - ADMIN

### 고려사항

- 이메일 중복 방지
- 비밀번호 정책
- 탈퇴 회원 처리
- Refresh Token Redis 저장
- 토큰 탈취 대응을 위한 회전 방식은 선택 구현

---

## 4.2 상품

### 필수 기능

- 상품 등록
- 상품 수정
- 상품 삭제 또는 판매 중지
- 상품 단건 조회
- 상품 목록 조회
- 카테고리 조회
- 상품명 검색
- 가격 범위 검색
- 정렬
  - 최신순
  - 가격순
  - 판매량순

### 데이터 예시

- 상품 ID
- 상품명
- 설명
- 가격
- 상태
- 카테고리
- 재고 수량
- 판매 수량
- 생성일
- 수정일

### 고려사항

- 상품 삭제는 물리 삭제보다 상태 변경을 우선 고려
- 상품 목록은 페이징 적용
- 조회 API에는 읽기 전용 트랜잭션 적용
- 자주 조회되는 상품은 Redis 캐시 적용
- 상품 수정 시 캐시 제거 또는 갱신

---

## 4.3 장바구니

### 필수 기능

- 장바구니 상품 추가
- 수량 변경
- 상품 삭제
- 장바구니 조회
- 전체 선택 주문

### 고려사항

- 판매 중지 상품 검증
- 현재 가격과 장바구니 저장 당시 가격 차이 처리
- 최대 주문 가능 수량 검증

---

## 4.4 주문

### 주문 상태

```text
CREATED
PAYMENT_PENDING
PAID
PREPARING
SHIPPED
COMPLETED
CANCELLED
FAILED
```

### 필수 기능

- 주문 생성
- 주문 상품 저장
- 주문 금액 계산
- 배송지 저장
- 주문 조회
- 주문 목록 조회
- 주문 취소
- 관리자 주문 상태 변경

### 주문 생성 흐름

1. 사용자 확인
2. 상품 존재 및 판매 상태 확인
3. 주문 수량 확인
4. 재고 확보
5. 쿠폰 검증
6. 총액 계산
7. 주문 및 주문 상품 저장
8. 결제 요청
9. 결제 성공 시 주문 상태 변경
10. 주문 완료 이벤트 발행

### 중요 고려사항

- 주문 당시 상품명과 가격은 주문 상품에 스냅샷으로 저장
- 금액 계산은 서버에서 수행
- 주문 요청 중복 방지
- 재고 정합성 보장
- 결제 실패 시 재고 복구
- 트랜잭션과 외부 시스템 호출 분리

---

## 4.5 결제

실제 PG사 대신 Mock Payment 서비스를 구현한다.

### 필수 기능

- 결제 요청
- 결제 성공
- 결제 실패
- 결제 취소
- 결제 내역 조회

### 고려사항

- 외부 결제 API 호출을 가정한 인터페이스 설계
- idempotency key를 이용한 중복 결제 방지
- 결제 성공 후 DB 반영 실패 상황 고려
- 재시도 정책
- 결제 이벤트 저장

---

## 4.6 재고

### 필수 기능

- 상품별 재고 관리
- 재고 차감
- 재고 복구
- 재고 부족 예외
- 관리자 재고 조정

### 동시성 처리

최소 두 가지 방식을 비교한다.

1. DB 비관적 락 또는 낙관적 락
2. Redis 분산 락

각 방식에 대해 다음을 문서화한다.

- 구현 방법
- 장점
- 단점
- 테스트 결과
- 처리량
- 실패 가능성
- 최종 선택 이유

### 동시성 테스트 예시

- 재고 100개
- 동시에 1,000건 주문 요청
- 성공 주문 수는 정확히 100건이어야 함
- 재고는 0 미만이 되면 안 됨

---

## 4.7 쿠폰

### 필수 기능

- 쿠폰 생성
- 쿠폰 발급
- 쿠폰 조회
- 쿠폰 사용
- 쿠폰 만료
- 쿠폰 사용 취소

### 할인 유형

- 정액 할인
- 정률 할인

### 고려사항

- 최소 주문 금액
- 최대 할인 금액
- 1인 1회 발급
- 발급 수량 제한
- 동시 발급 처리
- 주문 취소 시 쿠폰 복구

---

## 4.8 인기 상품

### 필수 기능

- 상품 조회 수 증가
- 판매량 집계
- 인기 상품 목록
- Redis Sorted Set 활용

### 집계 기준 예시

```text
score = 조회 수 × 1 + 장바구니 추가 수 × 3 + 판매 수 × 10
```

점수 계산 기준은 설정값으로 분리한다.

---

## 4.9 알림

Kafka 이벤트 소비를 통해 처리한다.

### 알림 유형

- 주문 완료 알림
- 결제 실패 알림
- 배송 시작 알림
- 쿠폰 만료 예정 알림

실제 이메일 전송 대신 로그 또는 Mock Mail Sender를 사용할 수 있다.

---

## 4.10 관리자

### 필수 기능

- 상품 등록 및 수정
- 재고 조정
- 주문 조회
- 주문 상태 변경
- 쿠폰 생성
- 회원 목록 조회
- 판매 통계 조회

---

## 5. Kafka 이벤트 설계

## 5.1 주요 이벤트

- OrderCreatedEvent
- PaymentCompletedEvent
- PaymentFailedEvent
- OrderCompletedEvent
- OrderCancelledEvent
- InventoryDeductedEvent
- CouponUsedEvent

## 5.2 이벤트 공통 필드

```json
{
  "eventId": "UUID",
  "eventType": "ORDER_COMPLETED",
  "aggregateId": "orderId",
  "occurredAt": "ISO-8601",
  "version": 1,
  "payload": {}
}
```

## 5.3 필수 고려사항

- at-least-once 전달
- 중복 이벤트 소비 방지
- 소비 처리 이력 테이블
- 실패 재시도
- Dead Letter Topic
- 이벤트 버전 관리
- 순서 보장이 필요한 경우 파티션 키 설계
- DB 커밋과 이벤트 발행의 불일치 문제

## 5.4 Outbox Pattern

주문 트랜잭션과 이벤트 발행의 정합성을 위해 Outbox Pattern을 구현한다.

### 흐름

1. 주문 데이터 저장
2. 같은 트랜잭션에서 Outbox 이벤트 저장
3. 별도 Publisher가 Outbox 조회
4. Kafka 발행
5. 발행 완료 상태 변경

초기에는 스케줄러 기반 발행으로 구현하고, 이후 CDC 방식은 문서에서 비교한다.

---

## 6. Redis 설계

## 6.1 사용 범위

- Refresh Token
- 상품 조회 캐시
- 인기 상품
- 분산 락
- API 중복 요청 방지
- 선택적으로 Rate Limit

## 6.2 캐시 전략

### Cache Aside

1. Redis 조회
2. 값이 없으면 DB 조회
3. Redis 저장
4. 응답

### 적용 대상

- 상품 단건 조회
- 카테고리 목록
- 인기 상품 목록

### 고려사항

- TTL
- 캐시 무효화
- Cache Stampede
- Cache Penetration
- Redis 장애 시 DB fallback
- 직렬화 방식

---

## 7. Spring Batch 설계

## 7.1 배치 작업

### 일별 판매 통계

- 전일 주문 집계
- 총 매출
- 주문 건수
- 판매 상품 수
- 취소 금액

### 인기 상품 재계산

- Redis와 DB 데이터를 기반으로 랭킹 재산정

### 쿠폰 만료

- 만료 시간이 지난 쿠폰 상태 변경

### 장기 미결제 주문 정리

- 일정 시간 이상 PAYMENT_PENDING인 주문 실패 처리
- 필요 시 재고 복구

## 7.2 배치 고려사항

- 재실행 가능성
- Job Parameter
- 중복 실행 방지
- Chunk 처리
- Skip
- Retry
- 실패 로그
- 실행 이력 조회

---

## 8. 데이터베이스 설계

## 8.1 주요 테이블

- members
- refresh_tokens 또는 Redis 저장
- categories
- products
- inventories
- carts
- cart_items
- orders
- order_items
- payments
- coupons
- member_coupons
- outbox_events
- processed_events
- daily_sales_statistics

## 8.2 공통 컬럼

```text
created_at
updated_at
created_by
updated_by
```

필요한 테이블에만 적용한다.

## 8.3 인덱스 설계 예시

- members(email) unique
- products(category_id, status, created_at)
- products(name)
- orders(member_id, created_at)
- orders(status, created_at)
- order_items(order_id)
- member_coupons(member_id, status, expired_at)
- outbox_events(status, created_at)

인덱스는 실제 조회 쿼리와 실행 계획을 기준으로 결정한다.

## 8.4 Flyway

- 모든 스키마 변경은 Flyway migration으로 관리
- 기존 migration 파일 수정 금지
- 변경 시 새 버전 파일 추가

---

## 9. API 설계 규칙

## 9.1 기본 경로

```text
/api/v1
```

## 9.2 응답 형식

### 성공

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-01-01T00:00:00"
}
```

### 실패

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품을 찾을 수 없습니다.",
    "fieldErrors": []
  },
  "timestamp": "2026-01-01T00:00:00"
}
```

## 9.3 HTTP Status

- 200 OK
- 201 Created
- 204 No Content
- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 500 Internal Server Error

## 9.4 페이징 응답

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "hasNext": true
}
```

---

## 10. 보안

### 필수 구현

- Spring Security
- BCrypt
- JWT 인증
- 권한 검증
- CORS 설정
- 입력값 검증
- 민감정보 로그 제외
- 관리자 API 접근 제한

### 추가 고려사항

- SQL Injection
- XSS
- CSRF
- 비밀번호 무차별 대입
- 토큰 만료
- Refresh Token 탈취
- API Rate Limit
- 환경변수로 Secret 관리
- 운영 설정 파일 Git 제외

---

## 11. 테스트 전략

## 11.1 단위 테스트

대상:

- 주문 금액 계산
- 쿠폰 할인 계산
- 주문 상태 변경
- 재고 차감
- 재고 복구
- 결제 상태 변경

## 11.2 통합 테스트

대상:

- 회원가입 및 로그인
- 상품 등록 및 조회
- 주문 생성
- 결제 성공 및 실패
- 주문 취소
- 쿠폰 사용
- 동시 재고 차감
- Kafka 이벤트 소비
- Batch 실행

## 11.3 테스트 원칙

- 테스트 이름은 동작과 기대 결과를 표현
- Given / When / Then 구조
- 외부 시스템은 Mock 또는 Testcontainer 사용
- 핵심 비즈니스 로직은 높은 커버리지 확보
- 커버리지 숫자 자체보다 중요한 시나리오 보장을 우선

---

## 12. 성능 테스트

## 12.1 측정 대상

- 상품 목록 조회
- 상품 검색
- 주문 목록 조회
- 주문 생성
- 재고 동시 차감
- 인기 상품 조회

## 12.2 도구

- k6 권장
- JMeter 대체 가능

## 12.3 결과 기록

성능 개선 문서는 아래 형식으로 작성한다.

```text
문제:
주문 목록 조회 평균 2.1초

원인:
orders와 order_items 조인 및 인덱스 부재

개선:
복합 인덱스 추가
DTO Projection 적용
불필요한 fetch join 제거

결과:
평균 2.1초 -> 180ms
P95 3.8초 -> 310ms
```

각 결과에는 테스트 조건을 반드시 명시한다.

- 데이터 건수
- 동시 사용자 수
- 요청 수
- 실행 환경
- DB 사양

---

## 13. 로깅과 모니터링

## 13.1 로그

- 요청 ID
- 사용자 ID
- 주문 ID
- 이벤트 ID
- 처리 시간
- 예외 코드

민감정보는 로그에 남기지 않는다.

## 13.2 Actuator

노출할 항목을 최소화한다.

- health
- metrics
- prometheus

## 13.3 대시보드

Grafana에서 다음을 확인할 수 있게 한다.

- API 요청 수
- API 평균 응답 시간
- 에러율
- JVM 메모리
- GC
- DB Connection Pool
- Kafka Consumer Lag
- Redis 상태

---

## 14. Docker Compose 구성

로컬에서 다음 서비스를 한 번에 실행할 수 있어야 한다.

```text
application
postgresql
redis
kafka
prometheus
grafana
```

초기 개발 단계에서는 애플리케이션을 IDE에서 실행하고 인프라만 Docker Compose로 실행해도 된다.

---

## 15. CI/CD

## 15.1 Pull Request

- 빌드
- 단위 테스트
- 통합 테스트
- 코드 스타일 검사

## 15.2 Main Branch Merge

- Docker 이미지 빌드
- 이미지 저장소 Push
- EC2 배포
- Health Check
- 실패 시 배포 중단

## 15.3 환경 분리

- local
- test
- dev
- prod

---

## 16. README 최종 구성

최종 README에는 반드시 다음 항목을 포함한다.

1. 프로젝트 소개
2. 개발 배경
3. 주요 기능
4. 기술 스택
5. 시스템 아키텍처
6. ERD
7. API 문서
8. 실행 방법
9. 테스트 방법
10. 성능 개선 사례
11. 동시성 문제 해결
12. Kafka 정합성 처리
13. 장애 대응 설계
14. 기술적 의사결정
15. 향후 개선 사항

---

## 17. 개발 단계

## Phase 0. 프로젝트 준비

- Git 저장소 생성
- Java 21 설정
- Spring Boot 프로젝트 생성
- Gradle 설정
- Docker Compose 작성
- PostgreSQL 연결
- Redis 연결
- 공통 응답
- 공통 예외 처리
- Swagger
- 기본 Health Check

### 완료 조건

- 애플리케이션 실행 성공
- PostgreSQL 연결 성공
- Redis 연결 성공
- Swagger 접속 성공
- 기본 테스트 성공

---

## Phase 1. 회원과 인증

- Member Entity
- 회원가입
- 로그인
- JWT
- Refresh Token
- 권한 처리
- 인증 테스트

### 완료 조건

- 회원가입 가능
- 로그인 가능
- 인증 API 접근 가능
- 권한별 접근 제어 가능
- 테스트 통과

---

## Phase 2. 상품과 카테고리

- Category
- Product
- Inventory
- 관리자 상품 CRUD
- 사용자 상품 조회
- 페이징
- 검색
- QueryDSL
- 상품 캐시

### 완료 조건

- 관리자만 상품 등록 가능
- 상품 검색과 정렬 가능
- 캐시 적용 전후 확인 가능
- N+1 문제가 없음

---

## Phase 3. 장바구니

- 장바구니 추가
- 수량 변경
- 삭제
- 조회
- 주문 가능 여부 검증

---

## Phase 4. 주문과 재고

- Order
- OrderItem
- 주문 생성
- 금액 계산
- 재고 차감
- 주문 조회
- 동시성 테스트

### 완료 조건

- 동시에 요청해도 재고가 음수가 되지 않음
- 성공 주문 수가 실제 재고 수와 일치
- 락 방식 비교 문서 작성

---

## Phase 5. 결제

- Mock Payment
- 결제 성공
- 결제 실패
- 결제 취소
- Idempotency Key
- 주문 상태 전이

---

## Phase 6. Kafka와 Outbox

- Kafka 설정
- 이벤트 발행
- 이벤트 소비
- 중복 소비 방지
- Outbox Pattern
- Retry
- DLT

---

## Phase 7. 쿠폰

- 쿠폰 생성
- 쿠폰 발급
- 쿠폰 사용
- 쿠폰 취소
- 동시 발급

---

## Phase 8. Spring Batch

- 일별 통계
- 쿠폰 만료
- 미결제 주문 정리
- Batch 실행 이력

---

## Phase 9. 운영 기능

- Actuator
- Prometheus
- Grafana
- 구조화 로그
- 장애 시나리오 테스트

---

## Phase 10. 배포

- AWS EC2
- RDS
- Nginx
- HTTPS
- GitHub Actions
- 운영 환경변수 설정
- 배포 문서 작성

---

## Phase 11. 포트폴리오 정리

- README
- ERD
- 아키텍처 다이어그램
- 성능 개선 자료
- 장애 대응 자료
- 기술 의사결정 기록
- 면접 예상 질문

---

## 18. Codex 작업 규칙

Codex는 매 작업 시 아래 규칙을 따른다.

1. 이 문서를 먼저 읽는다.
2. 현재 구현 단계가 무엇인지 확인한다.
3. 기존 코드를 먼저 분석한다.
4. 변경할 파일 목록을 먼저 제시한다.
5. 설계 이유를 간단히 설명한다.
6. 한 번에 너무 많은 기능을 구현하지 않는다.
7. 구현 후 테스트를 작성하거나 수정한다.
8. 실행 방법을 설명한다.
9. 완료되지 않은 부분을 명확히 표시한다.
10. 명세와 다른 구현이 필요하면 임의로 진행하지 말고 이유와 대안을 제시한다.

---

## 19. Codex 첫 요청 프롬프트

아래 문장을 VS Code Codex에 그대로 전달한다.

```text
PROJECT_SPEC.md를 먼저 전체적으로 읽어.

이 문서는 프로젝트의 최상위 요구사항이다.

현재는 Phase 0부터 시작한다.

아직 코드를 수정하지 말고 먼저 다음 내용을 제안해줘.

1. 전체 멀티모듈 또는 단일모듈 구조 중 어떤 방식이 적절한지
2. 추천 패키지 구조
3. build.gradle 의존성 목록
4. Docker Compose 구성
5. application 설정 분리 방식
6. Phase 0에서 생성하거나 수정할 파일 목록
7. 개발 순서
8. 예상되는 기술적 위험

과도한 추상화는 피하고, 포트폴리오이지만 실제 운영 가능한 구조를 기준으로 설명해줘.
```

---

## 20. 단계별 Codex 프롬프트 템플릿

```text
PROJECT_SPEC.md와 현재 코드를 읽어.

현재 단계는 Phase [번호]이다.

이번 작업 범위:
[구현할 기능]

먼저 다음을 설명해줘.

1. 현재 코드 상태
2. 이번 변경 설계
3. 변경할 파일
4. 트랜잭션 범위
5. 예외 상황
6. 테스트 시나리오

설명 후 코드를 구현해.

구현 후에는 다음을 정리해줘.

1. 변경된 파일
2. 실행 방법
3. 테스트 방법
4. 남은 작업
5. 다음 단계
```

---

## 21. Codex 코드 리뷰 프롬프트

```text
PROJECT_SPEC.md와 현재 구현을 기준으로 코드 리뷰를 진행해줘.

다음 항목을 중점적으로 확인해.

- 비즈니스 로직이 Controller에 있는지
- 트랜잭션 범위가 적절한지
- Entity가 API에 직접 노출되는지
- N+1 가능성이 있는지
- 동시성 문제가 있는지
- 예외 처리가 누락됐는지
- 테스트가 핵심 동작을 검증하는지
- Redis 장애 시 문제가 있는지
- Kafka 중복 소비 가능성이 있는지
- 보안상 위험한 코드가 있는지
- 불필요한 추상화가 있는지

문제별로 중요도를 Critical, High, Medium, Low로 구분하고
파일명과 수정 방향을 제시해줘.
```

---

## 22. 면접용 핵심 구현 우선순위

시간이 부족하면 아래 순서대로 완성한다.

1. 회원 인증
2. 상품 조회 및 관리자 상품 관리
3. 주문 생성
4. 재고 동시성
5. 결제 Mock
6. Redis 캐시
7. Kafka + Outbox
8. Spring Batch
9. 성능 테스트
10. AWS 배포
11. 모니터링

Kafka, Redis, Batch를 억지로 넣기보다 각각 왜 필요한지 설명할 수 있어야 한다.

---

## 23. 금지 사항

- 모든 기능을 한 번에 생성하지 않는다.
- 테스트 없이 핵심 기능을 완료 처리하지 않는다.
- Entity를 그대로 반환하지 않는다.
- 비밀번호와 토큰을 로그에 남기지 않는다.
- 외부 API 호출을 DB 트랜잭션 안에서 오래 수행하지 않는다.
- 재고를 단순 조회 후 감소하는 방식으로 구현하지 않는다.
- Kafka Consumer가 중복 실행되지 않는다고 가정하지 않는다.
- Redis가 항상 정상이라고 가정하지 않는다.
- 실행 계획 확인 없이 인덱스를 무작정 추가하지 않는다.
- 환경변수와 Secret을 Git에 커밋하지 않는다.

---

## 24. 최종 완료 기준

다음 조건을 충족하면 프로젝트를 완료로 본다.

- 로컬 Docker 환경 실행 가능
- 회원가입과 로그인 가능
- 상품 조회 및 관리 가능
- 주문 생성과 취소 가능
- 재고 동시성 보장
- 결제 성공과 실패 처리
- Redis 캐시 적용
- Kafka 이벤트 처리
- Outbox Pattern 적용
- Spring Batch 실행
- 테스트 자동화
- 성능 개선 결과 기록
- Swagger 제공
- 모니터링 대시보드 제공
- AWS 배포
- README와 기술 문서 작성
- 면접에서 주요 기술 선택 이유를 설명 가능

---

## 25. 프로젝트 문서 추가 권장 목록

프로젝트가 진행되면 아래 파일을 추가한다.

```text
README.md
PROJECT_SPEC.md
TODO.md
docs/
├── ARCHITECTURE.md
├── API.md
├── DATABASE.md
├── ERD.md
├── CONCURRENCY.md
├── KAFKA_OUTBOX.md
├── CACHE.md
├── PERFORMANCE.md
├── DEPLOYMENT.md
├── TROUBLESHOOTING.md
└── INTERVIEW.md
```

`PROJECT_SPEC.md`는 요구사항의 기준이며, 세부 구현 내용은 `docs` 아래 문서로 분리한다.
