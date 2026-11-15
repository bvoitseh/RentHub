# RentHub — Architecture

Map of the codebase. Keep it concise and accurate; update it in every feature branch that changes any of this.
Requirements live in `SPECIFICATION.md`, engineering rules in `CLAUDE.md`, recorded assumptions in `DECISIONS.md`.

## 1. Repository layout

```
renthub/
├─ renthub/     backend — Spring Boot 4.1 (Gradle), Java 25, package com.bohakka.renthub
├─ frontend/    SPA — React 19 + TypeScript + Vite 8, Tailwind CSS 4
├─ docs/        SPECIFICATION.md · ARCHITECTURE.md · DECISIONS.md
├─ docker-compose.yml   postgres · minio (+ bucket init) · mailpit
└─ .env.example         every environment variable the app reads
```

## 2. Backend

**Stack:** Spring Web MVC, Spring Data JPA (Hibernate 7), Spring Security 7, Bean Validation, Spring Mail,
Scheduling + `@Async`, Actuator, Flyway, springdoc-openapi (dev only), Lombok. H2 (PostgreSQL mode) in `dev`,
PostgreSQL in `prod`. No backend tests by project policy.

**Package-by-feature** under `com.bohakka.renthub`; each feature package uses `web/` (controllers + DTO records
+ mappers), `service/` (use cases, transactions, ownership checks), `repository/` (Spring Data), `domain/` (entities
and business rules).

| Package | Responsibility (status) |
|---|---|
| `config` | `SecurityConfig` (session + CSRF + BCrypt + method security), `SpaCsrfTokenRequestHandler`, `SecurityProblemHandler` (401/403 → advice), `TimeConfig` (`Clock`, Europe/Kyiv), `AsyncConfig`, `AppProperties` (`app.*`) |
| `common.error` | `DomainException` hierarchy (`NotFound` 404, `Conflict` 409, `Forbidden` 403, `BusinessRule` 422), `ApiExceptionHandler` (the only `@RestControllerAdvice`), `ProblemDetailFactory`, `ErrorCodes` |
| `admin` | `Settings` singleton entity (commission `feeRate`) + repository (done); categories, moderation, statistics (later) |
| `user`, `catalog`, `booking`, `review`, `complaint`, `recommendation` | planned, see CLAUDE.md §2.5 |

**Layering:** controller → service → repository. Controllers hold no business rules and never return entities;
DTOs are Java records mapped at the API boundary. JPA: lazy fetching, `open-in-view=false`.

**Where business rules will live:** availability and overbooking protection (`booking/domain` + service with
equipment row lock, FR-9/FR-11), booking state transitions (one enum-driven place, FR-14), pricing/commission
(`booking/domain`, reads `Settings.feeRate`, FR-10), season logic (`catalog/domain`), rating (`review`).

**Error contract:** every error is an RFC 9457 `ProblemDetail` (`application/problem+json`) with `status`, `title`,
Ukrainian `detail`, machine-readable `code` and, for validation, `errors: [{field, message}]`. Domain exceptions
carry status + code + message key (`messages_uk.properties`) + optional extra properties (e.g. conflicting days).
Security failures are routed through `HandlerExceptionResolver` so the same advice renders them.

**Security model (NFR-4):** server-side HTTP session, cookie `RENTHUB_SESSION` (HttpOnly, SameSite=Lax, Secure in
prod, 7-day timeout); CSRF double-submit cookie `XSRF-TOKEN` → header `X-XSRF-TOKEN` (SPA pattern from the Spring
Security reference); BCrypt; `@EnableMethodSecurity` for role checks; ownership checks in services. Default rule is
`anyRequest().authenticated()`; public paths are whitelisted explicitly (`/actuator/health`, dev tools in `dev`).

**Configuration & profiles:** `application.yml` (shared; secrets only via `${ENV}`), `application-dev.yml`
(file H2 `./data/renthub`, H2 console `/h2-console`, Swagger `/swagger-ui.html`, Mailpit), `application-prod.yml`
(PostgreSQL, secure cookie, forwarded headers for Nginx). Default profile is `dev`.

**Persistence:** Flyway `V{n}__{snake_case}.sql` in `src/main/resources/db/migration`, written to run unchanged on H2
(PostgreSQL mode) and PostgreSQL; Hibernate only validates (`ddl-auto=validate`). Timestamps are
`TIMESTAMP WITH TIME ZONE` ↔ `Instant`; money `NUMERIC(…, 2)` ↔ `BigDecimal`; dates `DATE` ↔ `LocalDate`.

**Time:** always via the injected `java.time.Clock` (Europe/Kyiv). JVM default locale is set to `uk`.

**Scheduled jobs:** none yet (booking completion/expiry and review publishing come with their features).

**Integrations:** none yet. Object storage, geocoding and e-mail will sit behind small application-owned interfaces
(one per capability) with a real and a stub/dev implementation chosen per profile.

## 3. Frontend

**Stack:** React 19, TypeScript strict, Vite 8, React Router 8 (data router), TanStack Query 5, React Hook Form +
Zod (added to the build, used from the accounts feature on), Tailwind CSS 4, Leaflet + react-leaflet (catalogue).
ESLint enforces `max-lines: 150` on every file.

```
src/
├─ main.tsx, index.css          entry, Tailwind theme (brand colours)
├─ app/        App.tsx (providers) · router.tsx (route table) · queryClient.ts
├─ api/        client.ts (fetch wrapper: JSON, same-origin cookies, CSRF header, ProblemError) · problem.ts · csrf.ts · health.ts
├─ components/layout/  AppLayout · Header · Footer · BackendStatus
├─ components/ui/      Button · Spinner/LoadingState · Alert · ErrorState · EmptyState
├─ pages/      HomePage · NotFoundPage (pages only compose components)
├─ i18n/uk.ts  all Ukrainian UI texts (split per feature when near 150 lines)
└─ features/<feature>/{components,hooks,api,schemas}   (added per feature)
```

Conventions: path alias `@/` → `src/`; API calls only through `src/api` or `features/*/api`; server state via
TanStack Query hooks (`useXxx`); every request renders loading / empty / error states (NFR-6); errors show the
backend's Ukrainian `detail` via `problemMessage()`.

Dev: Vite on :5173 proxies `/api` and `/actuator` to Spring Boot on :8080 (same origin, so session + CSRF cookies
work without CORS). Prod: Nginx serves `dist/` and proxies `/api`.

## 4. Patterns in use (one line each)

- **Repository** (Spring Data) — persistence behind interfaces, no SQL in services.
- **DTO + mapper at the boundary** — entities never leave the service layer.
- **Single exception advice** — one place turns exceptions into the problem contract.
- **Container/presentational + custom hooks** (frontend) — data fetching in hooks, rendering in small components.
