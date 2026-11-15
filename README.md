# RentHub

Web platform for peer-to-peer equipment rental (construction tools, photo/video gear, specialised equipment).
Owners publish listings, renters book them for specific dates; the platform guarantees no double booking,
recommends equipment nearby and in season, and lets both sides rate each other.

- Requirements: [`docs/SPECIFICATION.md`](docs/SPECIFICATION.md)
- Architecture map: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- Decisions log: [`docs/DECISIONS.md`](docs/DECISIONS.md)

## Stack

| Part | Technology |
|---|---|
| Backend (`renthub/`) | Java 25, Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Mail, Actuator), Flyway, embedded H2 (PostgreSQL mode), Gradle |
| Frontend (`frontend/`) | React 19, TypeScript, Vite 8, React Router, TanStack Query, React Hook Form + Zod, Tailwind CSS 4, Leaflet |
| Infrastructure | Docker Compose: MinIO (S3), Mailpit (SMTP catcher) |

## Prerequisites

- JDK 25, Node.js 22+ (24 recommended), npm 10+
- Docker Desktop (only for MinIO / Mailpit)

## Run locally

```bash
# 1. optional infrastructure (Mailpit UI: http://localhost:8025, MinIO console: http://localhost:9001)
docker compose up -d minio mailpit

# 2. backend — embedded H2 (file ./renthub/data/renthub), Flyway migrations, demo accounts
cd renthub && ./gradlew bootRun               # Windows: gradlew.bat bootRun

# 3. frontend — Vite dev server with /api proxy to :8080
cd frontend && npm install && npm run dev     # http://localhost:5173
```

Useful dev URLs:

| URL | What |
|---|---|
| http://localhost:5173 | the SPA |
| http://localhost:8080/actuator/health | health endpoint |
| http://localhost:8080/swagger-ui.html | OpenAPI UI |
| http://localhost:8080/h2-console | H2 console (JDBC URL: `jdbc:h2:file:./data/renthub;MODE=PostgreSQL`, user `sa`, empty password) |

The application needs no environment variables. Copy `.env.example` to `.env` to override anything
(admin bootstrap, SMTP, S3, …); the file lists every variable the application reads. `./gradlew bootRun` exports the
root `.env` to the application automatically (variables already set in the shell take precedence).

Demo accounts (created at start-up if missing; password `Password123` for all):

| E-mail | Role |
|---|---|
| `renthub.noreply+admin@gmail.com` | administrator |
| `renthub.noreply+owner@gmail.com` | owner, private person |
| `renthub.noreply+company@gmail.com` | owner, company (ТОВ «Будпрокат», ЄДРПОУ 12345678) |
| `renthub.noreply+renter@gmail.com` | renter |

Verification and password-reset e-mails are sent to Mailpit (`docker compose up -d mailpit`, UI at
http://localhost:8025). Without Mailpit the request still succeeds and the failed send is logged.
An administrator is also created from `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` when they are set and no admin exists.

## Build and checks

```bash
cd renthub  && ./gradlew build                # compiles and packages the backend (no tests by policy)
cd frontend && npm run lint && npm run build  # ESLint (150-line limit per file) + type-check + production bundle
```

## Configuration

One configuration for everything, no Spring profiles (educational project, see `docs/DECISIONS.md` D-17): embedded H2
in PostgreSQL mode, H2 console, Swagger UI, Mailpit on `localhost:1025` unless `SMTP_*` point elsewhere.

## Git workflow

`main` (releases) ← `develop` (integration) ← `feature/<name>`. Conventional Commits with FR/NFR references.
See `CLAUDE.md` §1.4.
