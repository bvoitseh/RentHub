# RentHub — Architecture

Map of the codebase. Keep it concise and accurate; update it in every feature branch that changes any of this.
Requirements live in `SPECIFICATION.md`, engineering rules in `CLAUDE.md`, recorded assumptions in `DECISIONS.md`.

## 1. Repository layout

```
renthub/
├─ renthub/     backend — Spring Boot 4.1 (Gradle), Java 25, package com.bohakka.renthub
├─ frontend/    SPA — React 19 + TypeScript + Vite 8, Tailwind CSS 4
├─ docs/        SPECIFICATION.md · ARCHITECTURE.md · DECISIONS.md
├─ docker-compose.yml   redis · minio (+ bucket init)
└─ dev.env              local environment (git-ignored); the keys are listed in the README
```

## 2. Backend

**Stack:** Spring Web MVC, Spring Data JPA (Hibernate 7), Spring Security 7, Bean Validation, Spring Mail,
Scheduling + `@Async`, Actuator, Flyway, Spring Data Redis, Lombok. Embedded H2 in PostgreSQL mode, one configuration
without Spring profiles (D-17). No backend tests by project policy.

**Package-by-layer** under `com.bohakka.renthub` (D-22): one monolith, one flat package per technical layer.
New features add classes to these packages; a new package is created only for a genuinely new kind of class.

| Package | Contents |
|---|---|
| `config` | `@Configuration` classes only: `SecurityConfig` (session + CSRF + BCrypt + method security), `TimeConfig` (`Clock`, Europe/Kyiv), `AsyncConfig`, `AdminAccountConfig` (start-up runner presetting the single admin, FR-2a) |
| `properties` | `@ConfigurationProperties` records: `AppProperties` (`app.*`), `RateLimitProperties` (`app.rate-limit.*`), `AdminBootstrapProperties` (`app.admin.*`) |
| `controller` | REST controllers: `AuthController` (`/api/v1/auth`), `MeController` (`/api/v1/users/me`) |
| `dto` | request/response records (`RegisterRequest`, `LoginRequest`, `UserResponse`, …), shared `Validation` constants, `EmailMessage` |
| `entity` | JPA entities and their enums: `User`, `Role`, `OwnerType` |
| `repository` | Spring Data repositories: `UserRepository` |
| `service` | service interfaces, the API other beans inject: `RegistrationService`, `LoginService`, `EmailVerificationService`, `PasswordResetService`, `AccountService`, `EmailTokenService` (+ `EmailTokenType` with validity), `RateLimiter` (NFR-14), `MailGateway`; plus `AccountMailListener` (event listener, no interface) |
| `service.impl` | the `@Service`/`@Component` implementations: `*ServiceImpl`, `RateLimiterImpl` (Redis), `EmailTokenServiceImpl` (Redis), `SmtpMailGateway` |
| `security` | Spring Security glue: `AppUserDetails`, `AppUserDetailsService`, `SessionEstablisher`, `SessionTerminator`, `SecurityProblemHandler` (401/403 → advice), `SpaCsrfTokenRequestHandler` |
| `exception` | `DomainException` hierarchy (`NotFound` 404, `AuthenticationFailed` 401, `Forbidden` 403, `Conflict` 409, `BusinessRule` 422, `RateLimited` 429), `ApiExceptionHandler` (the only `@RestControllerAdvice`), `ProblemDetailFactory`, `ErrorCodes` |
| `event` | application events that trigger e-mails: `VerificationEmailRequested`, `PasswordResetEmailRequested` |

**Layering:** controller → service → repository. Controllers hold no business rules and never return entities;
DTOs are Java records mapped at the API boundary. JPA: lazy fetching, `open-in-view=false`.

**Where business rules will live:** availability and overbooking protection (booking service with equipment row
lock, FR-9/FR-11), booking state transitions (one enum-driven place in `entity`, FR-14), pricing/commission (service,
FR-10; where the rate is stored is decided in that feature), season logic and rating in their services; entity methods guard their own invariants.

**Error contract:** every error is an RFC 9457 `ProblemDetail` (`application/problem+json`) with `status`, `title`,
Ukrainian `detail`, machine-readable `code` and, for validation, `errors: [{field, message}]`. Domain exceptions
carry status + code + message key (`messages_uk.properties`) + optional extra properties (e.g. conflicting days).
Security failures are routed through `HandlerExceptionResolver` so the same advice renders them.

**Security model (NFR-4):** server-side HTTP session, cookie `RENTHUB_SESSION` (HttpOnly, SameSite=Lax, 7-day
timeout); CSRF double-submit cookie `XSRF-TOKEN` → header `X-XSRF-TOKEN` (SPA pattern from the Spring
Security reference; the token rotates on logout); BCrypt; `@EnableMethodSecurity` for role checks; ownership checks
in services. Default rule is `anyRequest().authenticated()`; public paths are whitelisted explicitly
(`/actuator/health`, `POST /api/v1/auth/{register,login,restore,verify-email,forgot-password,reset-password}`,
H2 console). Login is a REST endpoint: `LoginService` authenticates through `AuthenticationManager`
(`DaoAuthenticationProvider` over `AppUserDetailsService`), checks blocked / deactivated **after** the password
(so a wrong password reveals nothing), then `SessionEstablisher` rotates the session id, stores the context in the
session and registers the session in Spring's in-memory `SessionRegistry`. `SessionTerminator` expires all sessions of
a user (password reset, deactivation, later admin block); `ConcurrentSessionFilter` answers 401 on an expired one.
Logout is Spring Security's filter at `POST /api/v1/auth/logout` (204). Rate limits (NFR-14) are enforced in the
services via `RateLimiter`, keyed per IP and per e-mail, stored in Redis (sorted set of hits + lock key with TTL,
updated by one Lua script so concurrent requests cannot overshoot the limit).

**Accounts (FR-1…FR-3c) in short:** `User` owns the rules (owner type / company fields, verify, deactivate, restore);
`EmailTokenService` issues 256-bit random tokens and keeps only their SHA-256 in Redis
(`email-token:{TYPE}:{hash}` → user id, plus a per-user pointer so a new token voids the old one); expiry is the Redis
TTL (verification 24 h, reset 1 h) and single use is the atomic `GETDEL` on consumption. E-mails go through application events (`VerificationEmailRequested`,
`PasswordResetEmailRequested`) handled by `AccountMailListener` with `@Async @TransactionalEventListener`
(after commit, off the request thread, failures logged). `AdminAccountConfig` (FR-2a) presets the single administrator at start-up.

**Configuration:** a single `application.yml`, no Spring profiles (D-17): file H2 `./data/renthub`, H2 console
`/h2-console`, SMTP relay (Gmail defaults); secrets and external services only via `${ENV}`
variables or the root `dev.env` (git-ignored, imported by `spring.config.import`; keys listed in the README).

**Persistence:** Flyway `V{n}__{snake_case}.sql` in `src/main/resources/db/migration`, written in portable SQL that
runs on H2 in PostgreSQL mode (and would on PostgreSQL); Hibernate only validates (`ddl-auto=validate`). Timestamps are
`TIMESTAMP WITH TIME ZONE` ↔ `Instant`; money `NUMERIC(…, 2)` ↔ `BigDecimal`; dates `DATE` ↔ `LocalDate`.

**Time:** always via the injected `java.time.Clock` (Europe/Kyiv). JVM default locale is set to `uk`.

**Scheduled jobs:** none yet (Redis TTLs replace clean-up jobs). Booking completion/expiry and review publishing
come with their features.

**Integrations:** e-mail via `service.MailGateway` (SMTP implementation; relay configured by `SMTP_*`, Gmail defaults).
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

- **Interface + implementation for services** (D-25) — controllers and services inject interfaces; Spring wires the single `service.impl` bean.
- **Repository** (Spring Data) — persistence behind interfaces, no SQL in services.
- **DTO + mapper at the boundary** — entities never leave the service layer.
- **Single exception advice** — one place turns exceptions into the problem contract.
- **Container/presentational + custom hooks** (frontend) — data fetching in hooks, rendering in small components.
