# CLAUDE.md — RentHub

Instructions for the agent implementing **RentHub**, a web platform for peer-to-peer equipment rental.

## 0. Read the specification first

- **`docs/SPECIFICATION.md` is the single source of truth for requirements** — domain, functional requirements (FR-n), non-functional requirements (NFR-n), actors and permissions, API outline. **Read it, together with `docs/ARCHITECTURE.md`, at the start of every session and before starting any feature**, and re-read the relevant sections before changing behaviour.
- Implement exactly what the specification says. If it is ambiguous or contradicts itself, do not invent features: pick the simplest option (KISS, YAGNI), record the assumption in `docs/DECISIONS.md` (with the FR/NFR it concerns) and tell the author. Do not edit `docs/SPECIFICATION.md` unless the author asks.
- Reference FR/NFR IDs in commit messages and PR descriptions.
- Language: code, comments, commits and docs are in **English**; the **user interface and all end-user messages (validation, errors, e-mails) are in Ukrainian**, kept in one place (`src/i18n/uk.ts` on the frontend, `messages_uk.properties` on the backend).

---

## 1. Critical rules (highest priority — never violate)

1. **Frontend is built strictly from components. No source file may exceed 150 lines of code** (`.tsx`, `.ts`, `.css` — every file, including pages, hooks, API modules and tests). If a file approaches the limit, split it before adding more.
   - Pages only compose components and hold routing-level wiring; no large inline JSX.
   - One component per file; extract sub-components (forms, cards, tables, list rows, dialogs, empty/loading/error states) into their own files.
   - Extract logic into custom hooks (`useXxx`), API calls into `src/api/*`, validation schemas into separate files, constants and Ukrainian texts into `src/i18n/uk.ts` (split it per feature, e.g. `src/i18n/uk/booking.ts`, if it nears the limit).
   - Group by feature: `src/features/<feature>/{components,hooks,api,schemas}`; shared UI primitives in `src/components/ui/`.
   - Before finishing any frontend task, check file lengths (e.g. `find frontend/src -name '*.ts*' | xargs wc -l | sort -n | tail`) and refactor anything over 150.
2. **No tests on the backend.** Do not write unit, integration, concurrency or Testcontainers tests, and do not add test dependencies, coverage plugins or coverage gates (no JaCoCo) to the backend. The author tests everything end to end personally and will request fixes. Backend code must still be correct and safe by construction (transactions, row locking, DB constraints, validation, ownership checks).

3. **Design is yours; follow SOLID and use design patterns only where they earn their place.** The entities, packages, class names and endpoints in this file describe the *domain and behaviour*, not a mandatory class diagram — do not copy them one-to-one into code. Model the domain the way a good engineer would, and rename, merge, split or add classes as the design requires.
   - **SOLID:** single responsibility per class (no god-services; split by use case/capability); open/closed for things that really vary; Liskov-safe abstractions; small, role-specific interfaces; depend on abstractions (constructor injection, no `new` of collaborators, no static state).
   - **Rich domain, thin edges:** business rules (availability, booking state transitions, price/commission calculation, season logic, rating) live in the domain/service layer, expressed in small cohesive types — not in controllers, repositories or React components.
   - **Patterns only when they solve a concrete problem in this code.** Good fits here: *Repository* (Spring Data), *DTO + mapper* at the API boundary, *Strategy* where an algorithm genuinely has interchangeable variants (e.g. recommendation ranking), *State*-style/enum-driven transitions for the booking lifecycle if it keeps rules in one place, *Specification*/criteria objects for composable catalogue filters, *Observer* via Spring application events for decoupled side effects. Typical frontend fits: custom hooks, container/presentational split, composition over inheritance.
   - **Do not add patterns for their own sake:** no Factory/Builder/Singleton/Visitor/Abstract-anything without a real need, no interface with a single implementation "just in case", no extra layers or indirection. If plain code is clearer, use plain code (KISS, YAGNI). Every pattern you introduce must be justifiable in one sentence.

4. **Git workflow — mandatory.** All work is tracked in git, using three kinds of branches:
   - **`main`** — stable, release-ready code only. Never commit or push to it directly; it changes only through the big release PR from `develop` (below).
   - **`develop`** — integration branch. Never commit directly to it either; it changes only through feature PRs.
   - **`feature/<short-name>`** — one branch per feature or fix (fixes too: `feature/fix-<short-name>`), always created from an up-to-date `develop`, e.g. `feature/booking-availability`. One feature = one branch; do not mix features.
   - If `develop` does not exist yet, create it from `main` and tell the author.
   - Commit in small, logical steps with clear English messages (Conventional Commits: `feat(booking): …`, `fix(catalog): …`, `refactor(…)`, `chore(…)`).
   - **Feature flow:** `develop` → `feature/...` → work and commit → **stop and report to the author** (what was done, how to run/check it). **Do not push, open a pull request or merge until the author explicitly approves.** After approval: push the feature branch and open a PR **`feature/... → develop`** (e.g. `gh pr create --base develop`) with a concise description. Never merge it yourself — the author merges.
   - **Release flow:** when the author says the set of features is ready, open one large PR **`develop → main`** with a summary of all included features. Only on the author's explicit request; never merge it yourself.
   - Start the next feature from `develop`, on a new branch (if the previous feature PR is not merged yet, ask the author whether to branch from it). Never open a feature PR against `main`.
   - **Never add yourself (Claude / Claude Code / any AI) as an author or contributor — in any way.** No `Co-Authored-By:` trailers, no "Generated with Claude Code" lines or links in commit messages, PR titles/descriptions or code comments; no AI mentions in `README`, `CONTRIBUTORS`, `AUTHORS` or similar files. Do not change the git `user.name`/`user.email` configured by the author; commits must appear solely under the author's identity. To prevent automatic attribution, keep `.claude/settings.json` with `{"attribution": {"commit": "", "pr": ""}}` committed in the repository.
   - Never commit secrets, `.env` files, build output (`target/`, `node_modules/`, `dist/`); maintain a proper `.gitignore`. Never rewrite published history (no force-push) without the author's request.

5. **Free and open-source tools only — nothing billable, ever.** Every library, framework, Docker image, CLI, service and API used anywhere in the project (code, build, dev environment, docs, deployment instructions) must be free of charge and open source (OSI-approved licence such as MIT, Apache-2.0, BSD, EPL, LGPL/GPL). Concretely:
   - No paid plans, trials, credit-card sign-ups, usage-metered APIs, SaaS tiers that can start billing, or "free tier" services that require payment details.
   - External services may only be used when a free, self-hostable or no-account alternative is the default (e.g. Mailpit / MinIO / PostgreSQL in Docker, OpenStreetMap Nominatim and tiles within their usage policies). Any third-party service is reached only through configuration (environment variables), never hard-wired, so the author can pick a provider later.
   - Before proposing any new dependency, image or service, state its licence and confirm it is free in the plan. If no free option exists for something, say so and leave the decision to the author — do not pick a paid one.

---

## 2. Working process

### 2.1 Feature workflow
1. Read the relevant parts of `docs/SPECIFICATION.md` and `docs/ARCHITECTURE.md`; check `docs/DECISIONS.md`.
2. **Plan first.** Before writing code, post a short plan: which FR/NFR the feature covers, which modules/files it touches, new endpoints, DB migrations, new dependencies (if any), open questions.
   - **Large feature** (changes the DB schema, touches more than one module, adds a dependency, or is expected to change more than ~10 files) → **wait for the author's approval of the plan** before coding.
   - Small feature or fix → state the plan and proceed.
3. Create the `feature/...` branch from `develop` (rule 1.4) and implement in small commits.
4. Verify against the Definition of Done (2.2), update docs, then report (2.2) and wait for approval before pushing/opening the PR.

### 2.2 Definition of Done and report
A feature is done only when all of these hold:
- [ ] Every FR/NFR listed in the plan is implemented as specified; deviations are recorded in `docs/DECISIONS.md`.
- [ ] Backend builds (`./mvnw verify`); frontend passes `npm run lint` and `npm run build` without errors or new warnings.
- [ ] No frontend file exceeds 150 lines (rule 1.1).
- [ ] The application starts in the `dev` profile and you have walked through the main scenario of the feature yourself (via the UI or HTTP requests), including at least one error case.
- [ ] Ownership/role checks are in place for every new endpoint; no secrets, personal data or `passwordHash` in responses or logs.
- [ ] `docs/ARCHITECTURE.md`, `docs/DECISIONS.md`, `.env.example` and the README (run instructions) are updated where affected.

Report to the author in this format:
```
Feature: <name>  (branch feature/...)
Covers: FR-x, FR-y, NFR-z
Done: <3–7 bullets of what was implemented>
How to check: <steps / URLs / sample requests, demo accounts>
Assumptions & decisions: <links to DECISIONS.md entries, or "none">
Not done / known limitations: <or "none">
```

### 2.3 Scope boundaries
- Change only what the current feature needs. **No refactoring, renaming, reformatting or "improvements" of unrelated code**; if you see a problem elsewhere, mention it in the report instead.
- **No new dependencies (Maven or npm) without the author's approval** — propose them in the plan with a one-line justification. Pin exact versions; do not upgrade existing ones unless asked.
- Do not change the tech stack, project structure conventions, or the specification on your own.

### 2.4 Database migrations and data
- Flyway files: `V{n}__{snake_case_description}.sql` with sequential `n`; one logical change per migration.
- **Never edit or delete a migration that has been committed** — fix forward with a new migration.
- Migrations must work on both H2 (PostgreSQL mode) and PostgreSQL.
- Demo/seed data (demo accounts for each role, categories, ~150 listings in Lviv) is loaded **only in the `dev` profile** and never by production migrations.
- **Never run destructive commands without the author's explicit request:** dropping/truncating tables, deleting data, `docker compose down -v`, removing volumes or buckets, `git reset --hard`, deleting branches.

### 2.5 Feature backlog (implementation order)
Implement in this order unless the author says otherwise; each item is one or more `feature/...` branches:
1. `feature/project-skeleton` — monorepo, Maven + Vite, Docker Compose (PostgreSQL, MinIO, Mailpit), profiles, Flyway, lint config, base layout, error handling.
2. `feature/accounts` — registration, login/logout, roles, owner type, e-mail verification, password reset, admin bootstrap, rate limits, profile, geo consent, deactivation/restore (FR-1…3c, FR-2a, NFR-14).
3. `feature/catalogue` — categories, listings CRUD, photos (S3), geocoding + map, quantity and rental terms, catalogue with filters/sorting, public profile (FR-4…7, FR-19 profile part).
4. `feature/availability-booking` — availability calendar and blocks, per-day availability, booking requests, confirm/reject/cancel, contacts, scheduler jobs (FR-8…15b).
5. `feature/notifications` — e-mail notifications and reminders (FR-15a, FR-18 reminders).
6. `feature/reviews` — blind reviews, window, replies, ratings (FR-18, FR-19).
7. `feature/recommendations` — location and season-aware recommendations on the home page (FR-16, FR-17).
8. `feature/complaints-moderation` — complaints, counters, blocking with auto-cancellation (FR-20, FR-21, FR-22 complaints).
9. `feature/admin` — categories and commission management, statistics E1–E5 (FR-22, FR-23).
10. `feature/production-setup` — Dockerfile, Nginx config, backups script, privacy-policy page, README deploy section (NFR-2, NFR-4, NFR-5).

### 2.6 Architecture document
Maintain **`docs/ARCHITECTURE.md`** (create it in the first feature): module/package map and responsibilities, key domain types and where business rules live (availability, booking state transitions, pricing, reviews), integrations and their interfaces, security model, scheduled jobs, frontend feature structure, and the patterns used with a one-line reason each. **Update it in every feature branch that changes any of this.** Read it at the start of every session — it is your map of the codebase; keep it concise (≤ 200 lines) and accurate rather than exhaustive.

---

## 3. Tech stack

**Backend**
- **Java 25**, Maven (wrapper `./mvnw`).
- **Spring Boot** — latest stable GA that officially supports Java 25 (check https://spring.io/projects/spring-boot; if in doubt use 3.5.x or 4.0.x).
- Spring Web (REST/JSON), Spring Data JPA (Hibernate), Spring Security, Bean Validation, Spring Scheduling, Actuator.
- **Flyway** migrations; **PostgreSQL** in production, **H2** (PostgreSQL mode) in the `dev` profile. Migrations must run unchanged on both databases. Overbooking protection relies on the equipment row lock + re-check inside one transaction (FR-11), which works the same on H2 and PostgreSQL; use `CHECK` constraints for simple invariants (`units ≥ 1`, `quantity ≥ 1`, `date_to ≥ date_from`, `rating BETWEEN 1 AND 5`).
- Auth: server-side session (HttpOnly, Secure, SameSite=Lax) + CSRF via `CookieCsrfTokenRepository`; same-origin SPA, Vite proxy `/api` in dev.
- Spring Mail (SMTP) for e-mails; AWS SDK for Java v2 S3 client (works with any S3-compatible storage, incl. MinIO); Spring `RestClient` for Nominatim; Spring application events + `@Async` for notifications.
- springdoc-openapi (Swagger UI in `dev` only). Java `record` for DTOs.
- No backend tests (see rule 1.2).

**Frontend**
- **React 19 + TypeScript (strict) + Vite**, React Router, TanStack Query, React Hook Form + Zod.
- Tailwind CSS or CSS Modules; responsive; no heavy UI frameworks.
- **Leaflet + react-leaflet** with OpenStreetMap tiles (keep the required attribution).
- ESLint with `max-lines` rule set to 150 (`"max-lines": ["error", {"max": 150, "skipBlankLines": true, "skipComments": true}]`) so the limit is enforced by the linter.

**Infrastructure**
- Docker Compose for local PostgreSQL, **MinIO** (S3) and **Mailpit** (SMTP catcher); backend Dockerfile.
- Production: S3-compatible bucket (any provider), transactional SMTP provider (free tier / own relay), all credentials via environment variables.
- Production: one VPS, Nginx (HTTPS, serves the built SPA, reverse-proxies `/api` to Spring Boot), daily `pg_dump`.
- Free/open-source dependencies only.

**Layout** (indicative — package-by-feature is recommended; exact packages and class names are your decision, see rule 1.3)
```
renthub/
├─ backend/   src/main/java/ua/renthub/{common,config,user,catalog,booking,review,complaint,recommendation,admin}
│             (each package: web/ · service/ · repository/ · domain/)
│             src/main/resources/db/migration
├─ frontend/  src/{app,routes,pages,features,components/ui,hooks,api,i18n,types}
│             src/features/<feature>/{components,hooks,api,schemas}   # auth, catalog, booking, owner, reviews, recommendations, admin
│             (every file ≤ 150 lines)
├─ docs/SPECIFICATION.md · docs/ARCHITECTURE.md · docs/DECISIONS.md
└─ docker-compose.yml
```
Layering: controller → service → repository. Controllers hold no business logic and never return JPA entities. JPA: `LAZY` fetching, `open-in-view=false`, avoid N+1.

**Commands**
```bash
cd backend  && ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev   # H2 + seed data
cd backend  && ./mvnw verify                                           # build (no tests)
cd frontend && npm install && npm run dev                              # Vite, proxy /api -> :8080
cd frontend && npm run lint && npm run build                         # lint enforces the 150-line limit
docker compose up -d postgres minio mailpit
```

**Engineering conventions**
- External integrations (object storage, geocoding, e-mail) sit behind small application-owned interfaces so they can be swapped or stubbed per profile.
- Money is `BigDecimal`; dates are `LocalDate`, timestamps `Instant`; time always comes from an injected `java.time.Clock` (zone Europe/Kyiv).
- Errors: domain exceptions mapped in one `@RestControllerAdvice` to RFC 9457 `ProblemDetail` with a machine-readable `code`.
- Secrets only via environment variables; commit a `.env.example`.
