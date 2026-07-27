# CommerceFlow

Java/Spring 기반 이커머스 백엔드 포트폴리오 프로젝트입니다.

## Current Phase

Phase 0. 프로젝트 준비

## Tech Stack

- Java 21
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- PostgreSQL
- Redis
- Flyway
- OpenAPI / Swagger
- Docker Compose

## Local Infrastructure

```bash
docker compose up -d
```

Docker Desktop must be running before executing Docker Compose commands on Windows.

## Build and Test

```bash
./gradlew test
```

On Windows PowerShell:

```powershell
.\gradlew.bat test
```

## API Docs

```text
http://localhost:8080/swagger-ui.html
```
