# Document Management Service (Backend)

A Spring Boot-based backend for a Document Management System (DMS). This project provides document ingestion, extraction (OCR/Tika), Vietnamese NLP (VnCoreNLP), full-text search indexing (Lucene), user authentication/authorization, file storage (Cloudinary / AWS S3), and REST APIs with OpenAPI documentation.

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Database & Migrations](#database--migrations)
- [NLP models](#nlp-models)
- [Running tests & building](#running-tests--building)
- [Development notes](#development-notes)
- [Contributing](#contributing)
- [License](#license)

## Features

- RESTful APIs for document management and search
- Authentication and authorization (Spring Security + JWT)
- Document parsing with Apache Tika and OCR (Tess4J)
- Vietnamese NLP pipeline using VnCoreNLP models (tokenization, POS, NER, dependency)
- Full-text search indexing (Apache Lucene) with custom stopwords (Vietnamese)
- File storage integrations: Cloudinary and AWS S3
- Flyway database migrations included
- OpenAPI documentation (springdoc)

## Tech stack

- Java 21
- Spring Boot 3.5.x
- Maven
- PostgreSQL
- Flyway
- Apache Tika, Tess4J (OCR)
- Apache Lucene
- VnCoreNLP (system-scoped JAR + models in resources)
- Cloudinary and AWS SDK (S3)
- SpringDoc OpenAPI UI

## Prerequisites

- JDK 21
- Maven 3.6+
- PostgreSQL (or a compatible database)
- (Optional) Docker and docker-compose if you prefer containerized DB/local services

Verify Java and Maven are available:

```bash
java -version
mvn -v
```

## Quick start

1. Clone the repository (if you haven't already):

```bash
git clone <repo-url>
cd document-management-be
```

2. Configure application properties. See `src/main/resources/application.properties` for properties used by the app (database, Flyway, cloudinary/S3, mail, JWT, etc.).

3. Prepare a PostgreSQL database and update `application.properties` (or use environment variables) with DB connection values.

4. Ensure VnCoreNLP jar and model files are present. This repo expects the system-scoped jar at `src/main/resources/vncorenlp/VnCoreNLP-1.2.jar` and model folders under `src/main/resources/vncorenlp/models/`.

5. Build and run:

```bash
mvn clean package
java -jar target/dms-0.0.1-SNAPSHOT.jar
```

Or run with Maven for development:

```bash
mvn spring-boot:run
```

6. Open the API docs (if enabled) at:

```
http://localhost:8080/swagger-ui.html  (or /swagger-ui/index.html depending on springdoc setup)
```

## Configuration

Primary configuration lives in `src/main/resources/application.properties`. Important sections include:

- spring.datasource.* — PostgreSQL connection properties
- spring.flyway.* — Flyway migration settings (migrations are in `src/main/resources/db/migration`)
- cloudinary.* and aws.s3.* — credentials/regions/bucket names for file storage
- jwt.* — secret, expiration and issuer for JWT tokens
- mail.* — SMTP settings used by notification/email templates

Sensitive values (credentials, secrets) should be provided via environment variables or a secrets manager in production. Do not commit secrets to source control.

## Database & Migrations

Flyway migrations are stored in `src/main/resources/db/migration`:

- V1__init_tables.sql
- V2__init_search_index.sql

Flyway will run automatically on application startup (check `application.properties`). To run migrations manually, use:

```bash
mvn -Dflyway.configFiles=src/main/resources/application.properties flyway:migrate
```

## NLP models

The application uses VnCoreNLP. The repository contains model folders under `src/main/resources/vncorenlp/models/` (POS, NER, dependency, wordsegmenter). The native jar dependency is referenced in `pom.xml` with a systemPath, so ensure `src/main/resources/vncorenlp/VnCoreNLP-1.2.jar` exists.

If you need to update models or the jar, follow VnCoreNLP installation instructions and replace files under `src/main/resources/vncorenlp/`.

## Running tests & building

Run unit/integration tests with Maven:

```bash
mvn test
```

Build the runnable JAR:

```bash
mvn clean package
```

If you only want to compile without tests:

```bash
mvn -DskipTests package
```

## Development notes

- The codebase is under `src/main/java/com/vpgh/dms/` with configurations in `config/`, controllers in `controller/`, entities and DTOs in `model/`, repositories in `repository/`, and services in `service/`.
- Search-related initialization and stopwords are in `src/main/resources/stopwords/`.
- Email templates are in `src/main/resources/templates/`.
- Static/native resources for NLP are under `src/main/resources/vncorenlp/`.

Common tasks:

- To run locally with a DB in Docker, create a docker-compose file (or use the repo's if present) and point `application.properties` to the container host/port.
- To test file upload and OCR, ensure Tesseract native binaries are available on the host (Tess4J requires Tesseract). Install Tesseract via your package manager and configure `TESSDATA_PREFIX` if needed.

Edge cases and caveats:

- VnCoreNLP is included as a system-scoped dependency. Ensure the jar and model files are present before packaging.
- OCR and Tika may require additional native dependencies (Tesseract). On CI, provide those dependencies or skip heavy integration tests.
