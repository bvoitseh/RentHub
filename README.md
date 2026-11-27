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
| Backend (`renthub/`) | Java 25, Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Mail, Actuator), Flyway, embedded H2 (PostgreSQL mode), Gradle |
| Frontend (`frontend/`) | React 19, TypeScript, Vite 8, React Router, TanStack Query, React Hook Form + Zod, Tailwind CSS 4, Leaflet |
| Infrastructure | Docker Compose: Redis (rate limits, e-mail link tokens, geocoding cache), SeaweedFS (S3 photo storage) |

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
| server / app | `SERVER_PORT`, `APP_BASE_URL` |
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

### Regions and settlements (reference data)

Regions and settlements of Ukraine (D-48) are reference data in migrations V11 and V12, generated from OpenStreetMap
(© OpenStreetMap contributors, ODbL 1.0) and Wikidata (legal status, CC0). The app never calls an external service for
them. To refresh, rarely and by hand:

```bash
python infra/osm/export_settlements.py      # Overpass export, resumable, cache in infra/osm/cache (git-ignored)
python infra/osm/wikidata_status.py         # legal status from Wikidata, resumable
python infra/osm/build_reference.py --report                     # counts per region, remaining ambiguous labels
python infra/osm/build_reference.py --refresh-sql renthub/src/main/resources/db/migration/V<next>__refresh_settlements.sql
```

The refresh migration matches rows by OSM id: new settlements are inserted, renamed or changed ones updated, and
those missing from the export marked inactive (hidden from search). Nothing is deleted, so profiles and listings keep
their settlement. Delete `infra/osm/cache` first to fetch fresh data; the scripts use only the Python standard library
and respect the Overpass / Wikidata usage policies (sequential queries, pauses, identifying User-Agent).

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
