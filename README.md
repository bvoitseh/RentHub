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
| Infrastructure | Docker Compose: MinIO (S3) |

## Prerequisites

- JDK 25, Node.js 22+ (24 recommended), npm 10+
- Docker Desktop (only for MinIO, catalogue photos)
- An SMTP account for outgoing e-mail (Gmail with an app password works)

## Run locally

```bash
# 1. optional infrastructure (MinIO console: http://localhost:9001)
docker compose up -d minio

# 2. backend — embedded H2 (file ./renthub/data/renthub), Flyway migrations
cd renthub && ./gradlew bootRun               # Windows: gradlew.bat bootRun

# 3. frontend — Vite dev server with /api proxy to :8080
cd frontend && npm install && npm run dev     # http://localhost:5173
```

Useful dev URLs:

| URL | What |
|---|---|
| http://localhost:5173 | the SPA |
| http://localhost:8080/actuator/health | health endpoint |
| http://localhost:8080/h2-console | H2 console (JDBC URL: `jdbc:h2:file:./data/renthub;MODE=PostgreSQL`, user `sa`, empty password) |

The application needs no environment variables except SMTP credentials for e-mail. Local configuration lives in
`dev.env` at the repository root (git-ignored, the single place for secrets). Spring Boot imports it itself
(`spring.config.import`), so it works from Gradle, IntelliJ and `java -jar` alike; the syntax is Java `.properties`
(`KEY=value`, no inline comments; comment a key out to keep the default), and real environment variables take
precedence. Keys it may contain:

| Group | Keys |
|---|---|
| server / app | `SERVER_PORT`, `APP_BASE_URL` |
| SMTP | `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS` |
| admin account (FR-2a) | `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` (optional) |
| rate limits (NFR-14) | `RATE_LIMIT_LOGIN`, `RATE_LIMIT_REGISTRATION`, `RATE_LIMIT_PASSWORD_RESET`, `RATE_LIMIT_RESEND_VERIFICATION` |
| object storage (later) | `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_PUBLIC_URL` |
| geocoding (later) | `NOMINATIM_URL`, `NOMINATIM_USER_AGENT` |

Verification and password-reset e-mails go out through the SMTP relay configured in `dev.env` (`SMTP_*`; Gmail
defaults, so only `SMTP_USERNAME` and `SMTP_PASSWORD` are required). A failed send is logged and the request
still succeeds.

The only preset account is the administrator: it is created at start-up from `ADMIN_EMAIL` and `ADMIN_PASSWORD` in
`dev.env` (`ADMIN_NAME` is optional, default «Адміністратор») when no administrator exists yet. There is only ever one:
later starts skip creation, and registration rejects the ADMIN role. Owners and renters register through the UI.

## Build and checks

```bash
cd renthub  && ./gradlew build                # compiles and packages the backend (no tests by policy)
cd frontend && npm run lint && npm run build  # ESLint (150-line limit per file) + type-check + production bundle
```

## Configuration

One configuration for everything, no Spring profiles (educational project, see `docs/DECISIONS.md` D-17): embedded H2
in PostgreSQL mode, H2 console, outgoing e-mail through the `SMTP_*` relay from `dev.env`.

## Git workflow

`main` (releases) ← `develop` (integration) ← `feature/<name>`. Conventional Commits with FR/NFR references.
See `CLAUDE.md` §1.4.
