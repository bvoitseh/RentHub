# RentHub

Web platform for peer-to-peer equipment rental (construction tools, photo/video gear, specialised equipment).
Any user can publish listings and book other users' equipment for specific dates for specific dates; the platform guarantees no double booking,
recommends equipment nearby and in season, and lets both sides rate each other.

- Requirements: [`docs/SPECIFICATION.md`](docs/SPECIFICATION.md)
- Architecture map: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md)
- Decisions log: [`docs/DECISIONS.md`](docs/DECISIONS.md)

## Stack

| Part | Technology |
|---|---|
| Backend (`renthub/`) | Java 25, Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Mail, Actuator), Flyway, embedded H2 (PostgreSQL mode) locally, PostgreSQL in production, Gradle |
| Frontend (`frontend/`) | React 19, TypeScript, Vite 8, React Router, TanStack Query, React Hook Form + Zod, Tailwind CSS 4, Leaflet |
| Infrastructure | Docker Compose: Redis (rate limits, e-mail link tokens, geocoding cache), SeaweedFS (S3 photo storage); production adds Nginx and the backend image |

## Prerequisites

- JDK 25, Node.js 22+ (24 recommended), npm 10+
- Docker Desktop (Redis and the S3 storage are required by the backend)
- An SMTP account for outgoing e-mail (Gmail with an app password works)

## Run locally

```bash
# 1. infrastructure: Redis + S3-compatible photo storage (SeaweedFS; s3-init creates the bucket once)
docker compose up -d redis s3 s3-init

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
| server / app | `SERVER_PORT`, `APP_BASE_URL`, `LOG_LEVEL` (default `DEBUG`), `SESSION_COOKIE_SECURE` (default `false`) |
| database (D-47) | `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (unset = local file H2), `H2_CONSOLE_ENABLED` (default `true`) |
| Redis | `REDIS_HOST`, `REDIS_PORT` (defaults `localhost`, `6379`) |
| SMTP | `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH`, `SMTP_STARTTLS` |
| admin account (FR-2a) | `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME` (optional) |
| rate limits (NFR-14) | `RATE_LIMIT_LOGIN`, `RATE_LIMIT_REGISTRATION`, `RATE_LIMIT_PASSWORD_RESET`, `RATE_LIMIT_RESEND_VERIFICATION` |
| object storage (FR-4a) | `S3_ENDPOINT`, `S3_REGION`, `S3_BUCKET`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_PUBLIC_URL` (defaults match docker-compose and `infra/seaweedfs/s3.json`) |
| geocoding (FR-4) | `NOMINATIM_URL`, `NOMINATIM_USER_AGENT` (Nominatim's policy requires an identifying User-Agent with contact info) |
| recommendations (FR-16, FR-17) | `START_CITY`, `START_CITY_LAT`, `START_CITY_LON` (Lviv), `RECOMMENDATION_RADIUS_KM` (25), `RECOMMENDATION_LIMIT` (12) |
| reviews (FR-18) | `REVIEW_WINDOW` (default 14d after completion), `REVIEW_REPLY_WINDOW` (14d after publication) |
| e-mail retries (FR-15a) | `MAIL_RETRY_ATTEMPTS` (default 3; pauses 10 s and 60 s are set in `application.yml`) |
| booking (FR-10, FR-15b) | `BOOKING_MIN_LEAD_DAYS` (default 1 = from tomorrow), `BOOKING_HORIZON_DAYS` (365), `BOOKING_REQUEST_TTL` (48h) |
| map tiles (frontend) | `VITE_MAP_TILE_URL` in `frontend/.env.local`, optional; OpenStreetMap tiles by default |

Verification and password-reset e-mails go out through the SMTP relay configured in `dev.env` (`SMTP_*`; Gmail
defaults, so only `SMTP_USERNAME` and `SMTP_PASSWORD` are required). A failed send is logged and the request
still succeeds.

The only preset account is the administrator: it is created at start-up from `ADMIN_EMAIL` and `ADMIN_PASSWORD` in
`dev.env` (`ADMIN_NAME` is optional, default «Адміністратор») when no administrator exists yet. There is only ever one:
later starts skip creation, and registration never creates an admin. Everyone else registers through the UI as a
user who can both rent and rent out (account type «Приватна особа» by default, «Компанія» optional).

### Demo data (optional)

`infra/seed/demo-data.sql` adds 6 verified demo owners (4 private persons, 2 companies) and 150 listings around Lviv
with placeholder photos. It is not a migration: run it by hand once; running it again changes nothing. The photo files
are uploaded to the bucket by `s3-init`, so start the infrastructure first (`docker compose up -d redis s3 s3-init`).

With the backend running, open http://localhost:8080/h2-console, connect (see above) and execute:

```sql
RUNSCRIPT FROM '../infra/seed/demo-data.sql' CHARSET 'UTF-8'
```

The path is relative to the backend's working directory (`renthub/`); use an absolute path if your IDE runs it from
elsewhere. Demo logins: `seed.olena@example.com`, `seed.andrii@example.com`, `seed.marta@example.com`,
`seed.taras@example.com`, `seed.budprokat@example.com` (company), `seed.fotozona@example.com` (company), all with the
password `Password123`. The example.com addresses never receive mail.

## Build and checks

```bash
cd renthub  && ./gradlew build                # compiles and packages the backend (no tests by policy)
cd frontend && npm run lint && npm run build  # ESLint (150-line limit per file) + type-check + production bundle
```

## Configuration

One configuration for everything, no Spring profiles (`docs/DECISIONS.md` D-17): locally embedded H2 in PostgreSQL
mode, H2 console, outgoing e-mail through the `SMTP_*` relay from `dev.env`. Production is the same application with
other environment variables (PostgreSQL, no H2 console, INFO logs; D-47), see "Deploy" below.

## Deploy (production)

One server (VPS) with Docker. `docker-compose.prod.yml` runs:

| Service | Image | Role |
|---|---|---|
| `frontend` | built from `frontend/Dockerfile` (Node → `nginx:1.28-alpine`) | serves the SPA on port 80, proxies `/api` to the backend and `/photos` (read-only) to SeaweedFS, security headers |
| `backend` | built from `renthub/Dockerfile` (`eclipse-temurin:25-jre-alpine`, non-root) | Spring Boot; healthcheck on `/actuator/health` (not exposed through Nginx) |
| `redis` | `redis:8.2.11-alpine` | rate limits, e-mail link tokens, geocoding cache |
| `s3`, `s3-init` | `chrislusf/seaweedfs:4.50` | photo storage with a persistent volume; `s3-init` creates the bucket |

PostgreSQL is **not** in the compose file: the backend connects to an existing PostgreSQL server (database already
created). On the first start Flyway creates all tables, so the database's `public` schema must be empty then.

1. Install Docker Engine with the Compose plugin; clone the repository (e.g. to `/opt/renthub`).
2. Create `prod.env` in the repository root (git-ignored, never commit it):

   ```properties
   DB_URL=jdbc:postgresql://host.docker.internal:5432/renthub
   DB_USERNAME=renthub_user
   DB_PASSWORD=...
   APP_BASE_URL=http://your-domain
   S3_PUBLIC_URL=http://your-domain/photos
   S3_ACCESS_KEY=...            # any random values; SeaweedFS is configured from them
   S3_SECRET_KEY=...
   ADMIN_EMAIL=...              # the preset administrator (FR-2a)
   ADMIN_PASSWORD=...
   SMTP_HOST=...                # SMTP relay; SMTP_PORT, SMTP_USERNAME, SMTP_PASSWORD, SMTP_AUTH, SMTP_STARTTLS
   NOMINATIM_USER_AGENT=RentHub/1.0 (contact e-mail)
   PG_CONTAINER=...             # for backups: PostgreSQL container, database and role
   PG_DB=renthub
   PG_USER=...
   ```

   Optional: `HTTP_PORT` (default 80), `LOG_LEVEL` (INFO), `S3_BUCKET`, `CSP_IMG_SRC_EXTRA` (origin of an external S3
   provider's public URL, added to the Content-Security-Policy), `BACKUP_DIR` (`./backups`), `RETENTION_DAYS` (14) and
   every key from the configuration table above.
3. Point the backend at PostgreSQL, one of:
   - **PostgreSQL published on the host** (e.g. a container started with `-p 5432:5432`): keep
     `DB_URL=jdbc:postgresql://host.docker.internal:5432/<db>`; the compose file maps `host.docker.internal` to the host
     (`extra_hosts: host-gateway`). On Linux PostgreSQL must listen on the Docker bridge address too and `pg_hba.conf`
     must allow the Docker subnet.
   - **PostgreSQL container on a Docker network:** set `PG_NETWORK=<network name>` and
     `DB_URL=jdbc:postgresql://<postgres container name>:5432/<db>`, then add the override file to every command:
     `-f docker-compose.prod.yml -f docker-compose.prod.pgnet.yml`.
4. Start: `docker compose --env-file prod.env -f docker-compose.prod.yml up -d --build`, then check
   `docker compose -f docker-compose.prod.yml ps` (backend `healthy`) and open `http://your-domain`.
5. Update: `git pull`, then the same `up -d --build`; Flyway applies new migrations on start.

### Backups and restore (NFR-2)

`infra/backup/backup.sh` writes `BACKUP_DIR/<db>_<date>.sql.gz` with `pg_dump` run inside the PostgreSQL container
(settings `PG_CONTAINER`, `PG_DB`, `PG_USER` from `prod.env`) and deletes dumps older than `RETENTION_DAYS` days. Daily
at 03:30 via cron:

```cron
30 3 * * * cd /opt/renthub && ./infra/backup/backup.sh >> /var/log/renthub-backup.log 2>&1
```

Restore — always into a **new** database first, never over the live one:

```bash
docker exec <pg container> createdb -U <role> renthub_restore
gunzip -c backups/renthub_<date>.sql.gz | docker exec -i <pg container> psql -U <role> -d renthub_restore -v ON_ERROR_STOP=1
docker exec <pg container> psql -U <role> -d renthub_restore -c "select count(*) from flyway_schema_history"
```

To put it into service, stop the stack, point `DB_URL` at `renthub_restore` (or rename the databases while the
stack is stopped) and start it again.

### Enabling HTTPS

Not configured yet: the site is served over plain HTTP (D-47). When a domain and certificate exist, follow the TODO in
`infra/nginx/renthub.conf.template` (listen on 443 with the certificate, redirect port 80, add HSTS), publish port 443
in `docker-compose.prod.yml`, and set `SESSION_COOKIE_SECURE=true` and `https://` URLs in `APP_BASE_URL` and
`S3_PUBLIC_URL`.

## Git workflow

`main` (releases) ← `develop` (integration) ← `feature/<name>`. Conventional Commits with FR/NFR references.
See `CLAUDE.md` §1.4.
