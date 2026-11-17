# RentHub — Decisions and assumptions

Record of choices made where the specification or `CLAUDE.md` was ambiguous, silent or overridden by the author.
Newest entries last. Reference the FR/NFR each one concerns.

## D-1 Backend build: Gradle, package `com.bohakka.renthub` (2026-11-15)

**Concerns:** CLAUDE.md §3 (Maven, `backend/`, `ua.renthub`).
**Decision:** the author generated the backend with Spring Initializr as a Gradle project in `renthub/` and asked to keep it.
The backend therefore lives in `renthub/` (not `backend/`), builds with the Gradle wrapper and uses the package
`com.bohakka.renthub`. The `./mvnw` commands in CLAUDE.md map to `./gradlew build` and `./gradlew bootRun`.

## D-2 Lombok is allowed (2026-11-15)

**Concerns:** CLAUDE.md §3 (stack list). **Decision:** author's request. Used sparingly: `@Getter`,
`@RequiredArgsConstructor` (constructor injection), `@Slf4j`, `@NoArgsConstructor(PROTECTED)` on entities.
DTOs stay Java records.

## D-3 Spring Boot 4.1.2 (2026-11-15)

**Concerns:** CLAUDE.md §3 ("3.5.x or 4.0.x if in doubt"). **Decision:** 4.1.2 is the current GA line that
supports Java 25 and is what the Initializr project already targeted. Consequences: Spring Security 7, Hibernate 7,
Flyway 12, Jackson 3, starter names `spring-boot-starter-webmvc` / `spring-boot-starter-flyway`.

## D-4 First commit on `main` (2026-11-15)

**Concerns:** CLAUDE.md §1.4. The repository had no commits, so `develop` could not be branched. One bootstrap
commit (`CLAUDE.md`, specification, `.gitignore`, `.claude/settings.json`) was made directly on `main`; `develop`
and `feature/project-skeleton` were created from it. No further direct commits to `main` or `develop`.

## D-5 Initializr test stub removed (2026-11-15)

**Concerns:** CLAUDE.md §1.2 (no backend tests). The generated `RenthubApplicationTests` and the test dependencies
were removed; the Gradle `test` task is disabled.

## D-6 Tailwind CSS over CSS Modules (2026-11-15)

**Concerns:** CLAUDE.md §3 ("Tailwind CSS or CSS Modules"). **Decision:** Tailwind 4 via the Vite plugin: utility
classes keep component files short (150-line rule) and avoid a parallel `.module.css` file per component.

## D-7 TypeScript 5.9.x, not 7.x (2026-11-15)

**Concerns:** CLAUDE.md §3 (ESLint with `max-lines`). The current Vite template ships TypeScript 7 (native port) with
oxlint; `typescript-eslint` only supports TypeScript < 6.1, and the 150-line rule must be enforced by ESLint, so the
project pins TypeScript 5.9.3 and ESLint 10.

## D-8 Default security rule is "authenticated"; public paths are whitelisted (2026-11-15)

**Concerns:** NFR-4, NFR-11. Until the accounts feature adds login, every `/api/v1/**` request answers
`401 unauthenticated` as a problem detail. Guest-accessible endpoints (catalogue, availability, recommendations,
auth) will be whitelisted explicitly by their features, so nothing is public by accident.

## D-9 `settings` is the first migration (2026-11-15)

**Concerns:** FR-10, FR-22. The commission rate is needed by booking pricing and admin alike and is feature-agnostic,
so `V1__settings.sql` creates the singleton row (default 10 %). It also proves Flyway on H2 and PostgreSQL. Demo
seed data (accounts, categories, listings) comes with the respective features, `dev` profile only.

## D-10 Phone format and password length (2026-11-15)

**Concerns:** FR-1, NFR-6. The specification defines neither. **Decision:** phone must match the Ukrainian mobile
format `+380XXXXXXXXX`; password 8–72 characters (72 is BCrypt's input limit). Both are validated on the client (Zod)
and the server (Bean Validation) with the same Ukrainian messages.

## D-11 Registration reveals a taken e-mail; resend is for the logged-in user (2026-11-15)

**Concerns:** FR-1, NFR-14. Registering with an existing e-mail answers `409 user.email_taken`: without it the form
would be unusable. Forgot-password stays silent (`202` always). "Resend verification" takes no e-mail: it is
`POST /auth/resend-verification` for the authenticated user (an unverified user may log in, FR-1), so it cannot be
used to probe addresses. Both remain rate-limited per e-mail.

## D-12 FR-3c active-booking check is deferred to the booking feature (2026-11-15)

**Concerns:** FR-3c. Bookings do not exist yet, so `AccountService.deactivate` has an explicit, documented check point
(`assertNoActiveBookings`) that the availability-booking feature fills with the real query and the `409
user.has_active_bookings` response listing the bookings. Hiding a deactivated user's listings is done by the
catalogue feature's queries.

## D-13 `PUT /users/me` and auto-login after registration (2026-11-15)

**Concerns:** FR-1a ("the type is editable in the profile"), NFR-6. The API outline has no profile-edit endpoint, so
`PUT /users/me` (name, phone, city, owner type) was added. Registration opens a session immediately
(`201` + the user), so the new user lands on the site logged in, with the verification banner.

## D-14 Account state is checked after the password (2026-11-15)

**Concerns:** FR-2, FR-3c, NFR-14. Spring's default pre-authentication checks (locked/disabled) run before the
password check and would reveal "blocked" or "deactivated" to anyone knowing the e-mail. `AppUserDetails` therefore
reports every flag as enabled and `LoginService` checks `blocked` (403 `auth.blocked`) and `deactivated`
(409 `auth.deactivated`, which is what offers "Відновити акаунт") only after the credentials were verified.

## D-15 Sessions tracked in Spring's in-memory `SessionRegistry` (2026-11-15)

**Concerns:** FR-3a, FR-3c, FR-20. "Old sessions are invalidated" needs a way to find a user's sessions. The standard
`SessionRegistryImpl` + `ConcurrentSessionFilter` (unlimited sessions per user) is used; it is in-memory, consistent
with the single-instance assumption already made for rate limits (NFR-14). A restart forgets registrations but not
sessions, which is acceptable for this deployment.

## D-16 Admin has no phone or city; the dev seeder creates an admin (2026-11-15)

**Concerns:** FR-2a. The bootstrap admin comes from three variables only, so `users.phone` and `users.city` are
nullable in the schema and required by registration validation instead. In the `dev` profile `DemoUsersSeeder`
creates `admin@`, `owner@`, `company@` and `renter@renthub.local` (password `Password123`) regardless of the
`ADMIN_*` variables; `AdminBootstrap` still runs first and applies when the variables are set.

## D-17 One configuration, no Spring profiles, embedded H2 only (2026-11-15)

**Concerns:** CLAUDE.md §3 (PostgreSQL in production, H2 in `dev`), NFR-2/NFR-4/NFR-5 (production deployment),
backlog item 10. **Decision:** author's request — the project is educational and runs in one environment. The `dev`
and `prod` profiles were removed: a single `application.yml` configures the file-based H2 database (PostgreSQL mode),
the H2 console, Swagger UI and Mailpit as the default SMTP server; the PostgreSQL driver, the Flyway PostgreSQL module
and the `postgres` compose service are gone. Demo accounts are always seeded when missing. Migrations stay in portable
SQL, so PostgreSQL could be reintroduced later with a driver and a datasource URL. External services (SMTP, S3,
geocoding) remain configurable through environment variables; `./gradlew bootRun` exports the root `.env`.

## D-18 Demo accounts use real Gmail plus-addresses (2026-11-15)

**Concerns:** FR-1, FR-2a, NFR-13 (CLAUDE.md 2.4 demo data). **Decision:** author's request, so that e-mails for the
demo accounts arrive in a real inbox. The seeder creates `renthub.noreply+admin@gmail.com`, `+owner`, `+company` and
`+renter`; Gmail delivers all four to the `renthub.noreply@gmail.com` mailbox, which is also the SMTP sender in the
author's local `.env`. Previously seeded `@renthub.local` rows are not removed by the seeder.

## D-19 `dev.env` is the only environment file; no committed example (2026-11-15)

**Concerns:** CLAUDE.md §1.4 / §3 ("commit a `.env.example`"), NFR-4. **Decision:** author's request. The committed
`.env.example` was removed; the git-ignored `dev.env` at the repository root is the single source of truth for local
configuration and secrets, read by `./gradlew bootRun` (empty keys are skipped so `application.yml` defaults apply).
The list of keys is kept in the README so a fresh clone knows what the file may contain.
Update 2026-11-16: the file is imported by Spring Boot itself (`spring.config.import=optional:file:./dev.env
[.properties],optional:file:../dev.env[.properties]`) instead of a Gradle `bootRun` hook, so IntelliJ run
configurations and `java -jar` see it too; the Gradle loader was removed. `.properties` syntax: no inline comments,
optional keys commented out rather than left empty.

## D-20 No Mailpit; e-mail always goes through a real SMTP relay (2026-11-15)

**Concerns:** NFR-13 ("in dev, use MinIO and Mailpit"), CLAUDE.md §3. **Decision:** author's request. The Mailpit
service was removed from Docker Compose; `application.yml` defaults the relay to Gmail (`smtp.gmail.com:587`, auth +
STARTTLS) and `MAIL_FROM` to `SMTP_USERNAME`, so `dev.env` only needs the Gmail address and an app password. Demo
accounts are real plus-addresses (D-18), so every e-mail can be checked in the `renthub.noreply@gmail.com` inbox.

## D-21 No seeded demo accounts (2026-11-16)

**Concerns:** CLAUDE.md 2.4 (demo accounts for each role), FR-2a. **Decision:** author's request. `DemoUsersSeeder`
and the `dev` package were removed, which supersedes D-18. The only account created by the application is the first
administrator from `ADMIN_EMAIL` / `ADMIN_PASSWORD` / `ADMIN_NAME` (`AdminBootstrap`, FR-2a); every other account is
registered through the UI. Rows seeded by earlier runs stay in the local H2 file until deleted by hand.

## D-22 Package-by-layer instead of package-by-feature (2026-11-17)

**Concerns:** CLAUDE.md §3 layout (package-by-feature recommended). **Decision:** author's request: the application
is one monolith, so feature packages with their own `web/service/repository/domain` sub-packages added nesting
without benefit. All backend classes now live in flat layer packages: `config` (only `@Configuration` classes),
`properties` (`@ConfigurationProperties` records), `controller`, `dto`, `entity`, `repository`, `service`, `security`,
`exception`, `event`. Behaviour, endpoints and database are unchanged.

## D-23 Exactly one preset administrator, created by a `@Configuration` bean (2026-11-17)

**Concerns:** FR-2a. **Decision:** author's request. `config.AdminAccountConfig` declares an `ApplicationRunner` bean
that, inside one transaction, creates the administrator from `ADMIN_EMAIL` / `ADMIN_PASSWORD` when no ADMIN exists.
`ADMIN_NAME` became optional (default «Адміністратор»). If the e-mail already belongs to a non-admin account, nothing
is created and a warning is logged (no silent promotion). Registration rejects the ADMIN role, so the application
never holds more than one administrator. Supersedes the `AdminBootstrap` service.

## D-24 No Swagger / OpenAPI UI (2026-11-17)

**Concerns:** CLAUDE.md §3 (springdoc-openapi, Swagger UI in dev). **Decision:** author's request. The springdoc
dependency, its `application.yml` block and the `/swagger-ui/**` and `/api-docs/**` whitelist entries were removed.
The API is exercised through the SPA or plain HTTP clients (which must send the `X-XSRF-TOKEN` header on POST/PUT).

## D-25 Service beans are injected through interfaces (2026-11-17)

**Concerns:** CLAUDE.md §1.3 ("no interface with a single implementation just in case"). **Decision:** author's
request. Every service bean that another bean injects has an interface in `service` and its implementation in
`service.impl` (`XxxService` / `XxxServiceImpl`, `RateLimiter` / `RateLimiterImpl`, `MailGateway` /
`SmtpMailGateway`); controllers and services depend only on the interfaces. Beans that nothing injects
(`AccountMailListener`), `@Configuration` classes, repositories (already interfaces) and the Spring Security helpers
in `security` stay concrete.
