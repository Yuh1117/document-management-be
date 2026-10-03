# Document Management System (Backend)

A Spring Boot-based backend for a Document Management System (DMS). This project provides document and folder management, user authentication/authorization (JWT + Google OAuth), file storage (Cloudinary / Cloudflare R2), RabbitMQ integration, and REST APIs with OpenAPI documentation.

## System overview

This repository is one of three services that make up the DMS:

| Repository | Role |
|---|---|
| **document-management-be** (this repo) | Spring Boot REST API — auth, document/folder management, permissions, file storage, RabbitMQ publisher |
| **document-management-processor** | Python/FastAPI — OCR, chunking, embeddings, Elasticsearch indexing, local LLM summarization (Ollama), RabbitMQ worker |
| **document-management-fe** | Next.js 16 (App Router) frontend — UI, routing, admin panel, i18n |

Data flow:
1. The frontend calls the backend REST API to upload/manage documents.
2. The backend stores files in Cloudflare R2/Cloudinary, persists metadata in PostgreSQL, and publishes document events to RabbitMQ.
3. The processor consumes RabbitMQ events, runs OCR/parsing, chunking, generates embeddings, and indexes content into Elasticsearch.
4. Search and summarization requests from the frontend are proxied through the backend to the processor service, authenticated with a shared API key (`dms.processor.api-key`).

> This repository also owns `docker-compose.yml`, which orchestrates **all three
> services** plus their infrastructure. See [Docker Compose](#docker-compose).

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Docker Compose](#docker-compose)
- [Database & Migrations](#database--migrations)
- [Running tests & building](#running-tests--building)
- [Development notes](#development-notes)

## Features

- RESTful APIs for document and folder management (CRUD, versioning, copy/move, sharing)
- Authentication and authorization (Spring Security + JWT, Google OAuth)
- Role, permission, and user group management
- File storage integrations: Cloudinary and Cloudflare R2 (via the S3-compatible API) — migrated from AWS S3, see [File storage](#configuration)
- RabbitMQ publisher — triggers processor pipeline on document upload
- Proxies search, summarization, and model management requests to the processor service
- Summary feedback collection
- Email notifications (Spring Mail + Thymeleaf templates)
- Internationalization (EN/VI)
- Steganography support (StegoService)
- Flyway database migrations
- OpenAPI documentation (springdoc)

## Tech stack

- Java 25
- Spring Boot 3.5
- Gradle
- PostgreSQL
- Flyway
- Spring Security + OAuth2 Resource Server (JWT)
- Spring AMQP (RabbitMQ)
- Spring WebFlux (WebClient — processor communication)
- Cloudinary + AWS SDK for Java v2 (S3 client, pointed at Cloudflare R2)
- iText Core (PDF)
- Spring Mail + Thymeleaf (email templates)
- Google API Client (Google OAuth)
- SpringDoc OpenAPI UI

## Prerequisites

- JDK 25
- JDK 25 (the bundled Gradle wrapper handles the rest)
- PostgreSQL
- Running RabbitMQ instance
- Running processor service (for search/summarization)
- (Optional) Docker and docker-compose for containerized local services

Verify Java is available:

```bash
java -version
```

## Quick start

1. Clone the repository:

```bash
git clone <repo-url>
cd document-management-be
```

2. Configure `src/main/resources/application.yml` (or use environment variables). See [Configuration](#configuration) for required values.

3. Prepare a PostgreSQL database and set the DB connection properties.

4. Build and run:

```bash
./gradlew clean build
java -jar build/libs/dms-0.0.1-SNAPSHOT.jar
```

Or run with Gradle for development:

```bash
./gradlew bootRun
```

5. API docs are available at:

```
http://localhost:8080/swagger-ui/index.html
```

## Configuration

Primary configuration lives in `src/main/resources/application.yml`, which
imports a `.env` file via `spring.config.import`. Read that file for the
authoritative list of properties and their defaults.

Two things are easy to get wrong: the RabbitMQ, storage, and processor keys are
**custom properties**, not the Spring Boot defaults — `rabbitmq.host` rather than
`spring.rabbitmq.host`, and `r2.access-key` rather than `aws.s3.*`. And
`dms.processor.api-key` must match the processor's `PROCESSOR_API_KEY`, or every
search and summarize call will be rejected.

Do not commit secrets to source control. Use environment variables or a secrets manager in production.

### File storage: migrated from AWS S3 to Cloudflare R2

This project **used to store documents in AWS S3**. It now stores them in
[Cloudflare R2](https://developers.cloudflare.com/r2/) instead, accessed
through the same AWS SDK for Java v2 `S3Client`/`S3Presigner` since R2 exposes
an S3-compatible API — only the client configuration changed, not the
upload/download/copy/presign logic in `DocumentServiceImpl`.

`S3Configuration` (`config/S3Configuration.java`) points the client at R2 by:
overriding the endpoint, forcing path-style addressing, setting region to
`auto`, and relaxing checksum validation to `WHEN_REQUIRED` — R2 doesn't
support the newer AWS SDK default checksum behavior, and leaving it on will
cause uploads to fail.

Required `.env` values (bound via the `r2.*` properties in
`application.yml`):

| Variable | Description |
|---|---|
| `R2_ACCESS_KEY` / `R2_SECRET_KEY` | R2 API token credentials, created in the Cloudflare dashboard (**not** AWS IAM keys) |
| `R2_REGION` | Always `auto` for R2 |
| `R2_BUCKET_NAME` | Target R2 bucket name |
| `R2_ENDPOINT` | `https://<ACCOUNT_ID>.r2.cloudflarestorage.com` |

These replace the old `AWS_S3_ACCESS_KEY` / `AWS_S3_SECRET_KEY` / `AWS_S3_REGION`
/ `AWS_S3_BUCKET_NAME` variables and the `cloud.aws.*` / `aws.bucket.name`
properties from the S3 setup.

R2 doesn't support S3 object versioning, ACLs, tagging, or bucket policies —
this project doesn't rely on any of those, so it was a drop-in replacement.
Documents uploaded before the migration keep working from AWS S3
(`storage_type = 'AWS_S3'`); documents uploaded after the migration are stored
in R2 and tagged `storage_type = 'CLOUDFLARE_R2'`. The `document-management-processor`
service also reads these same `R2_*` variables to download files for OCR/indexing —
keep both `.env` files in sync.

### Build tool: migrated from Maven to Gradle

This project **used to build with Maven** (`pom.xml`, `mvnw`/`mvnw.cmd`). It now
builds with [Gradle](https://gradle.org/) (Kotlin DSL, `build.gradle.kts`) instead —
all dependencies and the Spring Boot plugin were carried over 1:1, only the build
tool changed, not the application code.

The Gradle Wrapper (`gradlew`/`gradlew.bat`) is pinned to **Gradle 9.1.0**, since
older Gradle versions (8.x) fail to start on JDK 25. Use the wrapper, not a
globally installed `gradle`, so everyone builds with the same version:

```bash
./gradlew clean build
./gradlew bootRun
./gradlew test
```

Command equivalents:

| Maven (old) | Gradle (new) |
|---|---|
| `mvn clean package` | `./gradlew clean build` |
| `mvn spring-boot:run` | `./gradlew bootRun` |
| `mvn test` | `./gradlew test` |
| `mvn -DskipTests package` | `./gradlew build -x test` |

`Dockerfile` and `Dockerfile.prod` were updated accordingly to run `./gradlew`
instead of `mvn`, and the build output moved from `target/*.jar` to
`build/libs/*.jar`. Flyway no longer has a standalone CLI task (`mvn flyway:migrate`
had no Gradle plugin equivalent added) — migrations still run automatically on
application startup.

## Docker Compose

`docker-compose.yml` in this repository brings up the whole system, including the
processor service built from `../document-management-processor`. The frontend is
**not** part of this compose file and is run separately.

```bash
# Core stack: postgres, rabbitmq, redis, elasticsearch, mlflow, backend, processor, worker
docker compose up -d

# Add Prometheus, Grafana and Kibana
docker compose --profile observability up -d

# One-off: register the summarization prompt/model into MLflow
docker compose --profile tools run --rm register
```

| Service | Port | Notes |
|---|---|---|
| backend | 8080 | This repo |
| processor | 8000 | FastAPI |
| worker | 8001 | RabbitMQ consumer; port exposes Prometheus metrics |
| postgres | 5432 | |
| rabbitmq | 5672 / 15672 | Management UI on 15672 |
| redis | 6379 | Embedding cache used by the processor |
| elasticsearch | 9200 | |
| mlflow | 5001 | Backed by SQLite in the `mlflow-data` volume |
| kibana | 5601 | `observability` profile |
| prometheus | 9090 | `observability` profile |
| grafana | 3000 | `observability` profile; default login `admin` / `admin` |

The `processor` and `worker` services request `gpus: all`. Remove that key from
`docker-compose.yml` if you are running without an NVIDIA runtime.

Prometheus scrapes only the `worker:8001` target, and the provisioned Grafana
dashboard (`monitoring/grafana/dashboards/processing-pipeline.json`) covers the
document-processing pipeline. The backend itself exposes no metrics endpoint —
there is no Actuator or Micrometer dependency in `build.gradle.kts`.

`docker-compose.prod.yml` mirrors the same topology with two differences: Kibana,
Prometheus and Grafana are **not** profile-gated and start with the default stack,
and it contains a commented-out `frontend` service.

## Database & Migrations

Flyway migrations are stored in `src/main/resources/db/migration`:

- `V1__init_tables.sql`

Flyway runs automatically on startup. To migrate manually:

Flyway does not have a Gradle CLI task configured in this project; it runs as
part of the Spring Boot application startup (`./gradlew bootRun`).

## Running tests & building

```bash
# Run tests
./gradlew test

# Build runnable JAR
./gradlew clean build

# Skip tests
./gradlew build -x test
```

## Development notes

- Source is under `src/main/java/com/vpgh/dms/` — `config/`, `controller/`, `model/`, `repository/`, `service/`, `util/`.
- Email templates are in `src/main/resources/templates/`.
- i18n message bundles: `messages.properties` (EN) and `messages_vi.properties` (VI).
- Controllers map roughly one-per-domain: `Auth`, `User`, `UserGroup`, `Role`,
  `Permission`, `SystemSetting`, `Folder`, `FolderShare`, `Document`,
  `DocumentShare`, `DocumentVersion`, `DocumentSummarize`, `SummaryFeedback`, `File`.
- Document and folder endpoints identify resources by **UUID**, not numeric ID.
