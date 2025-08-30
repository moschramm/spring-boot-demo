# Spring Boot Demo — REST + PostgreSQL + Docker Compose

A small Spring Boot demo application exposing REST CRUD endpoints backed by PostgreSQL. Built with Maven and prepared
for containerized deployment via Docker / Docker Compose.

## Features

- REST CRUD API: `/api/persons`
- Spring Data JPA or PostgreSQL (via Docker Compose)
- Spring Boot Actuator endpoints:
    - `/actuator/health`
    - `/actuator/metrics`
    - `/actuator/prometheus`
- Logging:
    - human-readable console + optional file for non-`prod` profile
    - JSON logs (Logstash encoder) for `prod` profile (stdout)
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
- Run (dev profile / default):
    - `java -jar target/demo-0.0.1-SNAPSHOT.jar`
- Run (production profile, JSON logs):
    - `java -Dspring.profiles.active=prod -jar target/demo-0.0.1-SNAPSHOT.jar`
- App: `http://localhost:8080/api/persons`
- Health: `http://localhost:8080/actuator/health`

## Run with Docker Compose (recommended for demo)

- Start:
    - `docker-compose up --build`
- Services:
    - App: `http://localhost:8080/api/persons`
    - Postgres: `localhost:5432` (data persisted in a Docker volume)

### Run App container with prod profile (JSON logs) via Docker Compose

- Option A — temporary env export:
  `export SPRING_PROFILES_ACTIVE=prod docker-compose up --build`
- Option B — `docker-compose.override.yml` (example):
  ```yaml
  services:
  app:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
  ```
- Option C — set env in docker-compose.yml:
  ```yaml
  services:
  app:
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - JDBC_DATABASE_URL=jdbc:postgresql://postgres:5432/demo
      - POSTGRES_USER=demo
      - POSTGRES_PASSWORD=demo
  ```

### Verify JSON logs

```shell
# start with prod profile then make a request
curl http://localhost:8080/api/persons
# check container logs or stdout for JSON lines
docker logs <container-id>
```

## Configuration

The application reads database connection values from environment variables. Defaults are compatible with
`docker-compose.yml` in this repo.

- `JDBC_DATABASE_URL` (e.g. `jdbc:postgresql://postgres:5432/demo`)
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

For local JVM run, you can export environment variables or override properties on the command line:

- Example:
  -
  `JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/demo POSTGRES_USER=demo POSTGRES_PASSWORD=demo java -jar target/demo-0.0.1-SNAPSHOT.jar`

## API examples

- Create:
  -
  `curl -X POST -H "Content-Type: application/json" -d '{"name":"Max","email":"max@example.com"}' http://localhost:8080/api/persons`
- List:
    - `curl http://localhost:8080/api/persons`
- Get by id:
    - `curl http://localhost:8080/api/persons/1`
- Update:
  -
  `curl -X PUT -H "Content-Type: application/json" -d '{"name":"Max Mustermann","email":"max2@example.com"}' http://localhost:8080/api/persons/1`
- Delete:
    - `curl -X DELETE http://localhost:8080/api/persons/1`
- Health:
    - `curl http://localhost:8080/actuator/health`
- Metrics:
    - `curl http://localhost:8080/actuator/metrics`
- Prometheus metrics:
    - `curl http://localhost:8080/actuator/prometheus`

## Dockerfile (summary)

This project uses a multi-stage Dockerfile: build with Maven image, copy resulting jar into a slim JRE base image. This
reduces final image size and separates build-time from runtime.

## CI / GitHub Actions

A minimal `.github/workflows/ci.yml` is included. It runs:

- Checkout
- Set up JDK 17
- `mvn -B -DskipTests package`
- `mvn test`

The workflow contains an optional image build-and-push job. To enable image push, add repository secrets (Repo →
Settings → Secrets and variables → Actions → New repository secret):

- `REGISTRY` (e.g. `ghcr.io` or `docker.io`)
- `REGISTRY_USERNAME`
- `REGISTRY_TOKEN`

Adjust the workflow to your registry and namespace.

## Actuator & metrics

- Exposed endpoints (configured in `application.properties`):
    - `management.endpoints.web.exposure.include=health,info,metrics,prometheus`
    - Base path: `/actuator`
- Useful endpoints:
    - `GET /actuator/health` — overall health
    - `GET /actuator/health/liveness` — liveness probe (if enabled)
    - `GET /actuator/health/readiness` — readiness probe (if enabled)
    - `GET /actuator/metrics` — available metrics
    - `GET /actuator/metrics/<metric.name>` — specific metric
    - `GET /actuator/prometheus` — Prometheus scrape endpoint
- Security note:
    - Do not expose all actuator endpoints in production without protection.
    - `management.endpoint.health.show-details=when_authorized` is configured so details require authorization.

## Kubernetes readiness / liveness examples

- Use actuator probe endpoints in your Pod spec:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

- If you run actuator on a separate management port, point probes to that port.

## Build & push Docker image (example)

- Build locally:

```shell
docker build -t your-namespace/demo:latest .
```

- Push (example to Docker Hub):

```shell
docker tag your-namespace/demo:latest docker.io/your-namespace/demo:latest
docker push docker.io/your-namespace/demo:latest
```

## Tests

- Run unit tests:

```shell
mvn test
```

- Example integration: start ```docker-compose up --build``` and run API calls as end-to-end smoke tests.

## Development notes & best practices

- Use `spring.profiles` for environment-specific configuration (e.g. `application-dev.properties` with H2).
- Use a migration tool (Flyway or Liquibase) for production schema changes — avoid
  `spring.jpa.hibernate.ddl-auto=update` in prod.
- Do not commit secrets; use environment variables, Docker secrets, or a secret manager.
- Add readiness/liveness checks when deploying to Kubernetes.
- Add resource requests/limits, logging configuration and monitoring (Micrometer → Prometheus / Grafana) for
  production-grade deployments.

## Quick troubleshooting

- If the app cannot connect to Postgres:
    - Check container logs: `docker-compose logs postgres`
    - Ensure DB service is reachable and credentials match environment variables.
- Rollback: with Docker Compose, stop the app, run previous image tag or local jar. For K8s deploys, use
  `kubectl rollout undo deployment/<name>`.

## Further improvements (suggestions)

- Add Micrometer + Prometheus exporter and a sample Grafana dashboard.
- Add Flyway migrations and a small seed dataset.
- Implement DTOs + validation (Hibernate Validator) and global exception handling.
- Add integration tests using Testcontainers (Postgres container) to increase confidence in CI.
