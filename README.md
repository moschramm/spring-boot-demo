# Spring Boot Demo — REST + PostgreSQL + Docker Compose

A small Spring Boot demo application exposing REST CRUD endpoints backed by PostgreSQL. Built with Maven and prepared for containerized deployment via Docker / Docker Compose.

## Features

- REST CRUD API: `/api/persons`
- Spring Data JPA (Postgres)
- Spring Boot Actuator (health, metrics)
- Multi-stage `Dockerfile` for small runtime image
- `docker-compose.yml` for local dev with a Postgres service
- Basic unit test and example GitHub Actions CI template

## Prerequisites

- Java 17 (or compatible JDK)
- Maven 3.6+
- Docker & Docker Compose (for containerized run)
- (Optional) GitHub account + Container Registry for CI image push

## Build & run (local, without Docker)

- Build:
    - `mvn clean package`
- Run:
    - `java -jar target/demo-0.0.1-SNAPSHOT.jar`
- App: `http://localhost:8080/api/persons`
- Health: `http://localhost:8080/actuator/health`

## Run with Docker Compose (recommended for demo)

- Start:
    - `docker-compose up --build`
- Services:
    - App: `http://localhost:8080/api/persons`
    - Postgres: `localhost:5432` (data persisted in a Docker volume)

## Configuration

The application reads database connection values from environment variables. Defaults are compatible with `docker-compose.yml` in this repo.

- `JDBC_DATABASE_URL` (e.g. `jdbc:postgresql://postgres:5432/demo`)
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

For local JVM run, you can export environment variables or override properties on the command line:

- Example:
    - `JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/demo POSTGRES_USER=demo POSTGRES_PASSWORD=demo java -jar target/demo-0.0.1-SNAPSHOT.jar`

## API examples

- Create:
    - `curl -X POST -H "Content-Type: application/json" -d '{"name":"Max","email":"max@example.com"}' http://localhost:8080/api/persons`
- List:
    - `curl http://localhost:8080/api/persons`
- Get by id:
    - `curl http://localhost:8080/api/persons/1`
- Update:
    - `curl -X PUT -H "Content-Type: application/json" -d '{"name":"Max Mustermann","email":"max2@example.com"}' http://localhost:8080/api/persons/1`
- Delete:
    - `curl -X DELETE http://localhost:8080/api/persons/1`
- Health:
    - `curl http://localhost:8080/actuator/health`
- Metrics:
    - `curl http://localhost:8080/actuator/metrics`

## Dockerfile (summary)

This project uses a multi-stage Dockerfile: build with Maven image, copy resulting jar into a slim JRE base image. This reduces final image size and separates build-time from runtime.

## CI / GitHub Actions

A minimal `.github/workflows/ci.yml` is included. It runs:

- Checkout
- Set up JDK 17
- `mvn -B -DskipTests package`
- `mvn test`

The workflow contains an optional image build-and-push job. To enable image push, add repository secrets (Repo → Settings → Secrets and variables → Actions → New repository secret):

- `REGISTRY` (e.g. `ghcr.io` or `docker.io`)
- `REGISTRY_USERNAME`
- `REGISTRY_TOKEN`

Adjust the workflow to your registry and namespace.

## Development notes & best practices

- Use `spring.profiles` for environment-specific configuration (e.g. `application-dev.properties` with H2).
- Use a migration tool (Flyway or Liquibase) for production schema changes — avoid `spring.jpa.hibernate.ddl-auto=update` in prod.
- Do not commit secrets; use environment variables, Docker secrets, or a secret manager.
- Add readiness/liveness checks when deploying to Kubernetes.
- Add resource requests/limits, logging configuration and monitoring (Micrometer → Prometheus / Grafana) for production-grade deployments.

## Quick troubleshooting

- If the app cannot connect to Postgres:
    - Check container logs: `docker-compose logs postgres`
    - Ensure DB service is reachable and credentials match environment variables.
- Rollback: with Docker Compose, stop the app, run previous image tag or local jar. For K8s deploys, use `kubectl rollout undo deployment/<name>`.

## Further improvements (suggestions)

- Add Micrometer + Prometheus exporter and a sample Grafana dashboard.
- Add Flyway migrations and a small seed dataset.
- Implement DTOs + validation (Hibernate Validator) and global exception handling.
- Add integration tests using Testcontainers (Postgres container) to increase confidence in CI.
