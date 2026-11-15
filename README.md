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
| Backend (`renthub/`) | Java 25, Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Mail, Actuator), Flyway, H2 (dev) / PostgreSQL (prod), Gradle |
| Frontend (`frontend/`) | React 19, TypeScript, Vite 8, React Router, TanStack Query, React Hook Form + Zod, Tailwind CSS 4, Leaflet |
| Infrastructure | Docker Compose: PostgreSQL, MinIO (S3), Mailpit (SMTP catcher) |

## Prerequisites

- JDK 25, Node.js 22+ (24 recommended), npm 10+
- Docker Desktop (only for MinIO / Mailpit / PostgreSQL)

## Run locally (dev profile)

```bash
# 1. optional infrastructure (Mailpit UI: http://localhost:8025, MinIO console: http://localhost:9001)
docker compose up -d minio mailpit            # add "postgres" only if you want to run the prod profile locally

# 2. backend — H2 (file ./renthub/data/renthub), Flyway migrations, dev tools
cd renthub && ./gradlew bootRun               # Windows: gradlew.bat bootRun

# 3. frontend — Vite dev server with /api proxy to :8080
cd frontend && npm install && npm run dev     # http://localhost:5173
```

Useful dev URLs:

| URL | What |
|---|---|
| http://localhost:5173 | the SPA |
| http://localhost:8080/actuator/health | health endpoint |
| http://localhost:8080/swagger-ui.html | OpenAPI UI (dev only) |
| http://localhost:8080/h2-console | H2 console (JDBC URL: `jdbc:h2:file:./data/renthub;MODE=PostgreSQL`, user `sa`, empty password) |

The dev profile needs no environment variables. Copy `.env.example` to `.env` to override anything
(admin bootstrap, SMTP, S3, …); the file lists every variable the application reads.

Demo accounts (dev profile only, created at start-up if missing; password `Password123` for all):

| E-mail | Role |
|---|---|
| `admin@renthub.local` | administrator |
| `owner@renthub.local` | owner, private person |
| `company@renthub.local` | owner, company (ТОВ «Будпрокат», ЄДРПОУ 12345678) |
| `renter@renthub.local` | renter |

Verification and password-reset e-mails are sent to Mailpit (`docker compose up -d mailpit`, UI at
http://localhost:8025). Without Mailpit the request still succeeds and the failed send is logged.
The first production administrator is created from `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` when no admin exists.

## Build and checks

```bash
cd renthub  && ./gradlew build                # compiles and packages the backend (no tests by policy)
cd frontend && npm run lint && npm run build  # ESLint (150-line limit per file) + type-check + production bundle
```

## Profiles

- `dev` (default): embedded H2 in PostgreSQL mode, H2 console, Swagger UI, Mailpit on `localhost:1025`.
- `prod`: PostgreSQL via `DB_URL`/`DB_USERNAME`/`DB_PASSWORD`, secure session cookie, no dev tools.
  Run with `./gradlew bootRun -Pprofile=prod` or `java -jar build/libs/renthub-*.jar --spring.profiles.active=prod`.

## Git workflow

`main` (releases) ← `develop` (integration) ← `feature/<name>`. Conventional Commits with FR/NFR references.
See `CLAUDE.md` §1.4.
