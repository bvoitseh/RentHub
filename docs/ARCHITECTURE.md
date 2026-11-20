# RentHub — Architecture

Map of the codebase. Keep it concise and accurate; update it in every feature branch that changes any of this.
Requirements live in `SPECIFICATION.md`, engineering rules in `CLAUDE.md`, recorded assumptions in `DECISIONS.md`.

## 1. Repository layout

```
renthub/
├─ renthub/     backend — Spring Boot 4.1 (Gradle), Java 25, package com.bohakka.renthub
├─ frontend/    SPA — React 19 + TypeScript + Vite 8, Tailwind CSS 4
├─ docs/        SPECIFICATION.md · ARCHITECTURE.md · DECISIONS.md
├─ docker-compose.yml   redis · s3 (SeaweedFS) + s3-init (bucket)
├─ infra/seaweedfs/     s3.json (local S3 identities) · create-bucket.sh (bucket + demo photos)
├─ infra/seed/          demo-data.sql (run by hand, D-36) · photos/ (placeholders uploaded by s3-init)
└─ dev.env              local environment (git-ignored); the keys are listed in the README
```

## 2. Backend

**Stack:** Spring Web MVC, Spring Data JPA (Hibernate 7), Spring Security 7, Bean Validation, Spring Mail,
Scheduling + `@Async`, Actuator, Flyway, Spring Data Redis, AWS SDK v2 (S3), Lombok. Embedded H2 in PostgreSQL mode, one configuration
without Spring profiles (D-17). No backend tests by project policy.

**Package-by-layer** under `com.bohakka.renthub` (D-22): one monolith, one flat package per technical layer.
New features add classes to these packages; a new package is created only for a genuinely new kind of class.

| Package | Contents |
|---|---|
| `config` | `@Configuration` classes only: `SecurityConfig` (session + CSRF + BCrypt + method security), `TimeConfig` (`Clock`, Europe/Kyiv), `AsyncConfig`, `RedisConfig` (Redis repositories + keyspace events for index clean-up on expiry), `AdminAccountConfig` (start-up runner presetting the single admin, FR-2a), `S3Config` (S3 client), `GeocodingConfig` (Nominatim `RestClient`) |
| `properties` | `@ConfigurationProperties` records: `AppProperties` (`app.*`), `RateLimitProperties` (`app.rate-limit.*`), `AdminBootstrapProperties` (`app.admin.*`), `StorageProperties` (`app.storage.*`), `GeocodingProperties` (`app.geocoding.*`) |
| `controller` | REST controllers: `AuthController` (`/api/v1/auth`), `MeController` (`/api/v1/users/me`), `UserProfileController` (`/api/v1/users/{id}`), `CategoryController`, `EquipmentController` (`/api/v1/equipment`, incl. `/my`), `EquipmentPhotoController`, `GeocodeController` |
| `dto` | request/response records (`RegisterRequest`, `UserResponse`, `EquipmentRequest`, `CatalogQuery`, `EquipmentSummaryResponse`, `EquipmentDetailsResponse`, `PageResponse`, `PublicProfileResponse`, …), shared `Validation` constants, `EmailMessage` |
| `entity` | JPA: `User` (+ `Role` = `USER`/`ADMIN`, `OwnerType` = account type), `Category`, `Equipment` (aggregate owning its `EquipmentPhoto`s, + `EquipmentStatus`, `ItemCondition`), value types `RentalTerms`, `GeoPoint`; Redis: `EmailToken` (+ `EmailTokenType`), `RateLimitAttempts`, `RateLimitBlock`, `GeocodeCacheEntry` |
| `repository` | Spring Data: `UserRepository`, `CategoryRepository`, `EquipmentRepository` (+ `JpaSpecificationExecutor`), `EquipmentPhotoRepository` (JPA); `EmailTokenRepository`, `RateLimitAttemptsRepository`, `RateLimitBlockRepository`, `GeocodeCacheRepository` (Redis); `EquipmentSpecifications` (catalogue filters) |
| `service` | service interfaces, the API other beans inject: `RegistrationService`, `LoginService`, `EmailVerificationService`, `PasswordResetService`, `AccountService`, `EmailTokenService`, `RateLimiter` (NFR-14), `MailGateway`, `CategoryService`, `CatalogService`, `ListingService`, `PhotoService`, `PublicProfileService`, `ObjectStorage`, `Geocoder`; listeners without interface: `AccountMailListener`, `StoredObjectCleanupListener` |
| `service.impl` | the `@Service`/`@Component` implementations: `*ServiceImpl`, `RateLimiterImpl`, `EmailTokenServiceImpl`, `SmtpMailGateway`, `S3ObjectStorage`, `NominatimGeocoder`; helpers `EquipmentMapper` (entity → DTO, photo URLs), `EquipmentAccess` (ownership / may-publish checks), `ImageFormat` (magic-byte sniffing), `CatalogSort` |
| `security` | Spring Security glue: `AppUserDetails`, `AppUserDetailsService`, `SessionEstablisher`, `SessionTerminator`, `SecurityProblemHandler` (401/403 → advice), `SpaCsrfTokenRequestHandler` |
| `exception` | `DomainException` hierarchy (`NotFound` 404, `AuthenticationFailed` 401, `Forbidden` 403, `Conflict` 409, `BusinessRule` 422, `RateLimited` 429), `ApiExceptionHandler` (the only `@RestControllerAdvice`), `ProblemDetailFactory`, `ErrorCodes` |
| `event` | application events: `VerificationEmailRequested`, `PasswordResetEmailRequested` (e-mails); `StoredObjectsRemoved`, `StoredObjectUploaded` (storage clean-up after commit / rollback) |

**Layering:** controller → service → repository. Controllers hold no business rules and never return entities;
DTOs are Java records mapped at the API boundary. JPA: lazy fetching, `open-in-view=false`.

**Catalogue (FR-4…FR-7) in short:** `Equipment` owns its rules: `RentalTerms` validates price, quantity, min/max days
and handover as one unit and knows which changes are frozen by active bookings (FR-5); photos are added/removed only
through the aggregate (1-5, contiguous positions, the last one cannot be removed); `archive()` keeps a blocked listing
blocked. `CatalogServiceImpl` composes `EquipmentSpecifications` (visible = ACTIVE + owner active + ≥ 1 photo; text,
category, price, rental length vs min/max days, units, radius, handover, owner type) and sorts by price, newest or
distance (portable SQL expression); cover photos of a page are loaded in one query. Photo files are sniffed by magic
bytes (JPEG/PNG/WebP, ≤ 5 MB), stored under `equipment/{id}/{uuid}.{ext}`, and removed by `StoredObjectCleanupListener`
after the commit (or after a rollback for a fresh upload). Not yet: free units on dates, booking-based FR-5 checks,
ratings and counters (D-32).

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
services via `RateLimiter`, keyed per IP and per e-mail, stored through Spring Data Redis repositories: `RateLimitAttempts` (counter whose TTL
starts with the first attempt and is read back on load) and `RateLimitBlock` (created when the limit is reached, TTL =
window); see D-29.

**Accounts (FR-1…FR-3c) in short:** `User` owns the rules (single `USER` role that rents and rents out, D-30; account type / company fields, verify,
deactivate, restore);
`EmailTokenService` issues 256-bit random tokens and keeps only their SHA-256, as the `@RedisHash` entity
`EmailToken` in the Spring Data Redis `EmailTokenRepository` (indexed by `userId` and `type`, so a new token voids the
old one); expiry is the entry's `@TimeToLive` (verification 24 h, reset 1 h), and keyspace events (`config.RedisConfig`)
let Spring Data drop expired tokens from the indexes. Consumption is find-then-delete (D-28). E-mails go through application events (`VerificationEmailRequested`,
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

**Integrations** (one small application-owned interface each, configured only through `${ENV}`):
e-mail via `MailGateway` (SMTP, `SMTP_*`); photos via `ObjectStorage` (`S3ObjectStorage`, AWS SDK v2 against any
S3-compatible service; SeaweedFS locally, `S3_*`, public-read bucket); geocoding via `Geocoder` (`NominatimGeocoder`:
identifying User-Agent, ≤ 1 request/s, answers cached in Redis 30 days, misses 1 day; a failure means "place the pin
by hand").

## 3. Frontend

**Stack:** React 19, TypeScript strict, Vite 8, React Router 8 (data router), TanStack Query 5, React Hook Form +
Zod, Tailwind CSS 4, Leaflet 1.9 used directly (no react-leaflet, D-34; OpenStreetMap tiles).
ESLint enforces `max-lines: 150` on every file.

```
src/
├─ main.tsx, index.css          entry, Tailwind theme (brand colours)
├─ app/        App.tsx (providers) · router.tsx (route table, auth pages lazy-loaded) · queryClient.ts
├─ api/        client.ts (fetch wrapper: JSON, same-origin cookies, CSRF header, ProblemError) · problem.ts · csrf.ts
│              · formErrors.ts (backend field violations → react-hook-form) · health.ts
├─ components/layout/  AppLayout · Header · Footer · BackendStatus
├─ components/ui/      Button · Input · Select · Checkbox · FormField · Dialog · Spinner/LoadingState · Alert · ErrorState · EmptyState
├─ components/map/     leaflet.ts (tiles, icon, helpers) · useLeafletMap (callback-ref map lifecycle) · MapView · PinPicker
├─ pages/      Home · NotFound · Login · Register · ForgotPassword · ResetPassword · VerifyEmail · Profile · Catalog ·
│              Equipment · PublicProfile · MyListings · ListingCreate · ListingEdit
├─ i18n/uk.ts  index composing src/i18n/uk/{auth,catalog,listings,profile,validation}.ts
└─ features/auth/   types.ts · api/{authApi,accountApi}.ts · hooks/{useCurrentUser,useAuthMutations,useAccountMutations,
                    usePasswordReset,useEmailVerification}.ts · schemas/*.ts (Zod) · components/ (forms, RestoreAccountDialog,
                    OwnerTypeFields, VerifyEmailBanner, UserMenu, RequireAuth, AuthCard, GeoConsentCard, DeactivateAccountCard)
├─ features/catalog/ types.ts · format.ts · api/catalogApi.ts · hooks/{useCatalogQueries,useCatalogFilters (URL state),
│                    useBrowserLocation} · components/ (search bar, filter panel, near-me, sort, results, cards, gallery,
│                    facts, owner panel, equipment view, public profile card, category links)
└─ features/listings/ types.ts · api/listingsApi.ts · hooks/useListings.ts · schemas/listingSchema.ts · components/
                     (ListingForm + basics/terms/location sections, PhotoManager, MyListingRow, ListingActions, ConfirmDialog)
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
- **Specification** (catalogue filters) — each FR-7 filter is one composable predicate; the service only picks which apply.
- **Observer** (storage clean-up) — transactional events delete stored files only after the database outcome is known.
