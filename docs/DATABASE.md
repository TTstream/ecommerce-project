# Database

이 문서는 CommerceFlow의 현재 데이터베이스 스키마와 마이그레이션 상태를 기록한다.

## Migration

스키마 변경은 Flyway migration으로 관리한다.

현재 migration은 다음과 같다.

```text
V1__init.sql
V2__create_members.sql
```

로컬 PostgreSQL 검증 결과:

```text
version 1: init, success
version 2: create members, success
```

기존 migration 파일은 수정하지 않는다. 스키마 변경이 필요하면 새 버전 파일을 추가한다.

## members

회원 기본 정보를 저장한다.

```sql
CREATE TABLE members
(
    id         BIGSERIAL PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    name       VARCHAR(50)  NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    status     VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP    NOT NULL,
    updated_at TIMESTAMP    NOT NULL,
    CONSTRAINT uk_members_email UNIQUE (email)
);
```

### Columns

```text
id: surrogate primary key
email: 로그인 식별자, unique
password: BCrypt encoded password
name: 회원 이름
role: USER 또는 ADMIN
status: ACTIVE 또는 WITHDRAWN
created_at: 생성 시각
updated_at: 수정 시각
```

### Constraints

```text
members_pkey: primary key on id
uk_members_email: unique constraint on email
```

이메일 중복은 application service에서 먼저 검사하고, DB unique constraint로 최종 방어한다.
