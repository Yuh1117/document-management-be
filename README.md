# Document Management System (Backend)

A Spring Boot-based backend for a Document Management System (DMS). This project provides document and folder management, user authentication/authorization (JWT + Google OAuth), file storage (Cloudinary / AWS S3), RabbitMQ integration, and REST APIs with OpenAPI documentation.

## System overview

This repository is one of three services that make up the DMS:

| Repository | Role |
|---|---|
| **document-management-be** (this repo) | Spring Boot REST API — auth, document/folder management, permissions, file storage, RabbitMQ publisher |
| **document-management-processor** | Python/FastAPI — OCR, chunking, embeddings, Elasticsearch indexing, Gemini summarization, RabbitMQ worker |
| **document-management-fe** | React + Vite frontend — UI, routing, admin panel, i18n |

Data flow:
1. The frontend calls the backend REST API to upload/manage documents.
2. The backend stores files in S3/Cloudinary, persists metadata in PostgreSQL, and publishes document events to RabbitMQ.
3. The processor consumes RabbitMQ events, runs OCR/parsing, chunking, generates embeddings, and indexes content into Elasticsearch.
4. Search and summarization requests from the frontend are proxied through the backend to the processor service.

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Database & Migrations](#database--migrations)
- [Running tests & building](#running-tests--building)
- [Development notes](#development-notes)

## Features

- RESTful APIs for document and folder management (CRUD, versioning, copy/move, sharing)
- Authentication and authorization (Spring Security + JWT, Google OAuth)
- Role, permission, and user group management
- File storage integrations: Cloudinary and AWS S3
- RabbitMQ publisher — triggers processor pipeline on document upload
- Proxies search and summarization requests to the processor service
- Summary feedback collection
- Email notifications (Spring Mail + Thymeleaf templates)
- Internationalization (EN/VI)
- Steganography support (StegoService)
- Flyway database migrations
- OpenAPI documentation (springdoc)

## Tech stack

- Java 21
- Spring Boot 3.5.x
- Maven
- PostgreSQL
- Flyway
- Spring Security + OAuth2 Resource Server (JWT)
- Spring AMQP (RabbitMQ)
- Spring WebFlux (WebClient — processor communication)
- Cloudinary + AWS SDK (S3)
- iText Core (PDF)
- Spring Mail + Thymeleaf (email templates)
- Google API Client (Google OAuth)
- SpringDoc OpenAPI UI

## Prerequisites

- JDK 21
- Maven 3.6+
- PostgreSQL
- Running RabbitMQ instance
- Running processor service (for search/summarization)
- (Optional) Docker and docker-compose for containerized local services

Verify Java and Maven are available:

```bash
java -version
mvn -v
```

## Quick start

1. Clone the repository:

```bash
git clone <repo-url>
cd document-management-be
```

2. Configure `src/main/resources/application.properties` (or use environment variables). See [Configuration](#configuration) for required values.

3. Prepare a PostgreSQL database and set the DB connection properties.

4. Build and run:

```bash
mvn clean package
java -jar target/dms-0.0.1-SNAPSHOT.jar
```

Or run with Maven for development:

```bash
mvn spring-boot:run
```

5. API docs are available at:

```
http://localhost:8080/swagger-ui/index.html
```

## Configuration

Primary configuration lives in `src/main/resources/application.properties`. Key sections:

- `spring.datasource.*` — PostgreSQL connection
- `spring.flyway.*` — Flyway migration settings
- `spring.rabbitmq.*` — RabbitMQ connection
- `cloudinary.*` and `aws.s3.*` — file storage credentials
- `jwt.*` — secret and expiration for JWT tokens
- `spring.mail.*` — SMTP settings for email notifications
- `processor.*` — base URL of the processor service

Do not commit secrets to source control. Use environment variables or a secrets manager in production.

## Database & Migrations

Flyway migrations are stored in `src/main/resources/db/migration`:

- `V1__init_tables.sql`
- `V2__add_mlops_columns.sql`

Flyway runs automatically on startup. To migrate manually:

```bash
mvn -Dflyway.configFiles=src/main/resources/application.properties flyway:migrate
```

## Running tests & building

```bash
# Run tests
mvn test

# Build runnable JAR
mvn clean package

# Skip tests
mvn -DskipTests package
```

## Development notes

- Source is under `src/main/java/com/vpgh/dms/` — `config/`, `controller/`, `model/`, `repository/`, `service/`.
- Email templates are in `src/main/resources/templates/`.
- i18n message bundles: `messages.properties` (EN) and `messages_vi.properties` (VI).
