# RentHub — Architecture

Map of the codebase. Keep it concise and accurate; update it in every feature branch that changes any of this.
Requirements live in `SPECIFICATION.md`, engineering rules in `CLAUDE.md`, recorded assumptions in `DECISIONS.md`.

## 1. Repository layout

```
renthub/
├─ renthub/     backend — Spring Boot 4.1 (Gradle), Java 25, package com.bohakka.renthub
├─ frontend/    SPA — React 19 + TypeScript + Vite 8, Tailwind CSS 4
├─ docs/        SPECIFICATION.md · ARCHITECTURE.md · DECISIONS.md
├─ docker-compose.yml   minio (+ bucket init) · mailpit
└─ dev.env              local environment (git-ignored); the keys are listed in the README
```

## 2. Backend

**Stack:** Spring Web MVC, Spring Data JPA (Hibernate 7), Spring Security 7, Bean Validation, Spring Mail,
Scheduling + `@Async`, Actuator, Flyway, springdoc-openapi, Lombok. Embedded H2 in PostgreSQL mode, one configuration
without Spring profiles (D-17). No backend tests by project policy.

**Package-by-feature** under `com.bohakka.renthub`; each feature package uses `web/` (controllers + DTO records
+ mappers), `service/` (use cases, transactions, ownership checks), `repository/` (Spring Data), `domain/` (entities
and business rules).

| Package | Responsibility (status) |
|---|---|
| `config` | `SecurityConfig` (session + CSRF + BCrypt + method security), `SpaCsrfTokenRequestHandler`, `SecurityProblemHandler` (401/403 → advice), `TimeConfig` (`Clock`, Europe/Kyiv), `AsyncConfig`, `AppProperties` (`app.*`) |
| `common.error` | `DomainException` hierarchy (`NotFound` 404, `AuthenticationFailed` 401, `Forbidden` 403, `Conflict` 409, `BusinessRule` 422), `ApiExceptionHandler` (the only `@RestControllerAdvice`), `ProblemDetailFactory`, `ErrorCodes` |
| `common.ratelimit` | `RateLimiter` (in-memory sliding window + lock, `Clock`-driven), `RateLimitProperties` (`app.rate-limit.*`), `RateLimitedException` 429 (NFR-14) |
| `mail` | `MailGateway` interface + `SmtpMailGateway` (`JavaMailSender`), `EmailMessage` record |
| `user` | accounts (FR-1…FR-3c): `domain` (`User`, `EmailToken`, `Role`, `OwnerType`), `security` (`AppUserDetails`, `AppUserDetailsService`, `SessionEstablisher`, `SessionTerminator`), `service` (`RegistrationService`, `LoginService`, `EmailVerificationService`, `PasswordResetService`, `AccountService`, `EmailTokenService`, `AdminBootstrap`, `AccountMailListener`), `event` (mail-trigger records), `web` (`AuthController`, `MeController`, request/response records) |
| `dev` | `DemoUsersSeeder` — demo accounts per role, created at start-up when missing |
| `admin` | `Settings` singleton entity (commission `feeRate`) + repository (done); categories, moderation, statistics (later) |
| `catalog`, `booking`, `review`, `complaint`, `recommendation` | planned, see CLAUDE.md §2.5 |

**Layering:** controller → service → repository. Controllers hold no business rules and never return entities;
DTOs are Java records mapped at the API boundary. JPA: lazy fetching, `open-in-view=false`.

**Where business rules will live:** availability and overbooking protection (`booking/domain` + service with
equipment row lock, FR-9/FR-11), booking state transitions (one enum-driven place, FR-14), pricing/commission
(`booking/domain`, reads `Settings.feeRate`, FR-10), season logic (`catalog/domain`), rating (`review`).

**Error contract:** every error is an RFC 9457 `ProblemDetail` (`application/problem+json`) with `status`, `title`,
Ukrainian `detail`, machine-readable `code` and, for validation, `errors: [{field, message}]`. Domain exceptions
carry status + code + message key (`messages_uk.properties`) + optional extra properties (e.g. conflicting days).
Security failures are routed through `HandlerExceptionResolver` so the same advice renders them.

**Security model (NFR-4):** server-side HTTP session, cookie `RENTHUB_SESSION` (HttpOnly, SameSite=Lax, 7-day
timeout); CSRF double-submit cookie `XSRF-TOKEN` → header `X-XSRF-TOKEN` (SPA pattern from the Spring
Security reference; the token rotates on logout); BCrypt; `@EnableMethodSecurity` for role checks; ownership checks
in services. Default rule is `anyRequest().authenticated()`; public paths are whitelisted explicitly
(`/actuator/health`, `POST /api/v1/auth/{register,login,restore,verify-email,forgot-password,reset-password}`,
H2 console and Swagger UI). Login is a REST endpoint: `LoginService` authenticates through `AuthenticationManager`
(`DaoAuthenticationProvider` over `AppUserDetailsService`), checks blocked / deactivated **after** the password
(so a wrong password reveals nothing), then `SessionEstablisher` rotates the session id, stores the context in the
session and registers the session in Spring's in-memory `SessionRegistry`. `SessionTerminator` expires all sessions of
a user (password reset, deactivation, later admin block); `ConcurrentSessionFilter` answers 401 on an expired one.
Logout is Spring Security's filter at `POST /api/v1/auth/logout` (204). Rate limits (NFR-14) are enforced in the
services via `RateLimiter`, keyed per IP and per e-mail.

**Accounts (FR-1…FR-3c) in short:** `User` owns the rules (owner type / company fields, verify, deactivate, restore);
`EmailTokenService` issues 256-bit random tokens, stores only SHA-256, one usable token per user and type, single
use; verification 24 h, reset 1 h. E-mails go through application events (`VerificationEmailRequested`,
`PasswordResetEmailRequested`) handled by `AccountMailListener` with `@Async @TransactionalEventListener`
(after commit, off the request thread, failures logged). `AdminBootstrap` (FR-2a) runs at start-up.

**Configuration:** a single `application.yml`, no Spring profiles (D-17): file H2 `./data/renthub`, H2 console
`/h2-console`, Swagger `/swagger-ui.html`, Mailpit by default; secrets and external services only via `${ENV}`
variables, which `./gradlew bootRun` exports from the root `dev.env` (git-ignored; keys listed in the README).

**Persistence:** Flyway `V{n}__{snake_case}.sql` in `src/main/resources/db/migration`, written in portable SQL that
runs on H2 in PostgreSQL mode (and would on PostgreSQL); Hibernate only validates (`ddl-auto=validate`). Timestamps are
`TIMESTAMP WITH TIME ZONE` ↔ `Instant`; money `NUMERIC(…, 2)` ↔ `BigDecimal`; dates `DATE` ↔ `LocalDate`.

**Time:** always via the injected `java.time.Clock` (Europe/Kyiv). JVM default locale is set to `uk`.

**Scheduled jobs:** `RateLimiter.evictIdleBuckets` (every 10 min). Booking completion/expiry and review publishing
come with their features.

**Integrations:** e-mail via `mail.MailGateway` (SMTP implementation; Mailpit by default, any relay via `SMTP_*`).
Object storage and geocoding will follow the same pattern: one small application-owned interface per capability.

## 3. Frontend

**Stack:** React 19, TypeScript strict, Vite 8, React Router 8 (data router), TanStack Query 5, React Hook Form +
Zod (added to the build, used from the accounts feature on), Tailwind CSS 4, Leaflet + react-leaflet (catalogue).
ESLint enforces `max-lines: 150` on every file.

```
src/
├─ main.tsx, index.css          entry, Tailwind theme (brand colours)
├─ app/        App.tsx (providers) · router.tsx (route table, auth pages lazy-loaded) · queryClient.ts
├─ api/        client.ts (fetch wrapper: JSON, same-origin cookies, CSRF header, ProblemError) · problem.ts · csrf.ts
│              · formErrors.ts (backend field violations → react-hook-form) · health.ts
├─ components/layout/  AppLayout · Header · Footer · BackendStatus
├─ components/ui/      Button · Input · Select · Checkbox · FormField · Dialog · Spinner/LoadingState · Alert · ErrorState · EmptyState
├─ pages/      Home · NotFound · Login · Register · ForgotPassword · ResetPassword · VerifyEmail · Profile
├─ i18n/uk.ts  index composing src/i18n/uk/{auth,profile,validation}.ts
└─ features/auth/   types.ts · api/{authApi,accountApi}.ts · hooks/{useCurrentUser,useAuthMutations,useAccountMutations,
                    usePasswordReset,useEmailVerification}.ts · schemas/*.ts (Zod) · components/ (forms, RestoreAccountDialog,
                    OwnerTypeFields, VerifyEmailBanner, UserMenu, RequireAuth, AuthCard, GeoConsentCard, DeactivateAccountCard)
```

Conventions: path alias `@/` → `src/`; API calls only through `src/api` or `features/*/api`; server state via
TanStack Query hooks (`useXxx`); every request renders loading / empty / error states (NFR-6); errors show the
backend's Ukrainian `detail` via `problemMessage()`. Auth state is the `['auth','me']` query (null for guests; a 401
is not an error); login/register/restore set it, logout and deactivation clear the whole cache. `RequireAuth` guards
routes (redirects guests to `/login` and back). Forms: react-hook-form + Zod schemas with Ukrainian messages, backend
violations applied with `applyServerErrors`.

Vite on :5173 proxies `/api` and `/actuator` to Spring Boot on :8080 (same origin, so session + CSRF cookies
work without CORS).

## 4. Patterns in use (one line each)

- **Repository** (Spring Data) — persistence behind interfaces, no SQL in services.
- **DTO + mapper at the boundary** — entities never leave the service layer.
- **Single exception advice** — one place turns exceptions into the problem contract.
- **Container/presentational + custom hooks** (frontend) — data fetching in hooks, rendering in small components.
