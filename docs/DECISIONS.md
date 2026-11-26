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

## D-26 No `Settings` entity until a feature needs it (2026-11-17)

**Concerns:** FR-10, FR-22; supersedes the Java part of the skeleton decision on the settings singleton. **Decision:**
author's request. The unused `Settings` entity (with `SINGLETON_ID`) and `SettingsRepository` were removed; how the
commission rate is stored and read is designed in the booking/admin features. Migration `V1__settings.sql` is
committed and already applied, so it stays (CLAUDE.md 2.4); the `settings` table remains in the database, unmapped
and unused.

## D-27 Redis for rate limits and e-mail link tokens (2026-11-17)

**Concerns:** NFR-14 (rate limits), FR-1 / FR-3a / NFR-5 (single-use e-mail links). **Decision:** author's request.
Both moved to Redis (`spring-boot-starter-data-redis`, Apache-2.0, with the Lettuce client, MIT; image
`redis:8.2.11-alpine`, used under its OSI-approved AGPLv3 option). Rate-limit state survives restarts and expires by
TTL instead of a scheduled job; e-mail tokens are Redis keys with TTL, consumed with `GETDEL`, so the `email_tokens`
table and its entity/repository were removed (migration `V3__drop_email_tokens.sql`). Redis is now required: without
it the health check reports DOWN and login/registration/reset answer 500. Consuming a token happens before the
database transaction commits; if that transaction fails, the link is spent and the user requests a new one.

## D-28 E-mail tokens through a Spring Data Redis repository (2026-11-17)

**Concerns:** NFR-5, FR-1, FR-3a; refines D-27. **Decision:** author's request, for the same repository style as JPA.
`EmailToken` is a `@RedisHash` entity (id = SHA-256 of the raw token, `@Indexed` `userId` and `type`, `@TimeToLive`
from `EmailTokenType`), stored by `EmailTokenRepository extends CrudRepository`. `config.RedisConfig` enables Redis
repositories with keyspace events (`ON_STARTUP` sets Redis `notify-keyspace-events`), so tokens expired by TTL are also
removed from the indexes (Spring keeps a short-lived `:phantom` copy for that). Consumption is find-then-delete and
therefore not atomic: two simultaneous requests with the same link could both succeed; both come from the link holder
and do the same thing, so this is accepted. The rate limiter stays on `StringRedisTemplate` + Lua, because counters on
a sorted set are not entity CRUD. Spring Data runs in strict mode (JPA + Redis); each repository is assigned by its
entity annotation (`@Entity` / `@RedisHash`).

## D-29 Rate limiter through Spring Data Redis repositories (2026-11-17)

**Concerns:** NFR-14; supersedes the template + Lua limiter of D-27/D-28. **Decision:** author's request, following
the author's attempts/blocks pattern. Per key "{scope}:{value}" (scopes `login`, `registration`, `password-reset`,
`resend-verification`; values are an IP or an e-mail), `RateLimitAttempts` (`@RedisHash("rate_limit_attempts")`)
counts attempts and expires one window after the first attempt (Spring Data reads the remaining TTL back into the
`@TimeToLive` field, so saving the incremented counter keeps the window); when the count reaches the limit, a
`RateLimitBlock` (`@RedisHash("rate_limit_blocks")`) is saved with TTL = window and the counter is deleted. A
successful login deletes the e-mail's counter. Differences from before: the window is fixed from the first attempt
instead of sliding, and read-modify-write is not atomic, so concurrent attempts on one key may be under-counted by a
few. Both are acceptable for brute-force protection at this scale.

## D-30 One USER role: every account can rent and rent out (2026-11-17)

**Concerns:** FR-1 (role chosen at registration), FR-1a, FR-2, §4 actors (`RENTER`, `OWNER`). **Decision:** author's
request. The roles `RENTER` and `OWNER` are merged into `USER`; roles are now `USER` and `ADMIN`. Registration no
longer asks for a role, and any active, logged-in user may both book equipment and publish listings (the spec's owner
already had every renter permission, so no capability is lost; booking one's own equipment stays forbidden). The FR-1a
owner type becomes the user's **account type**: every user has one (`PRIVATE` by default, `COMPANY` with company name
and ЄДРПОУ), chosen optionally at registration and editable in the profile; the admin has none. Migration
`V4__single_user_role.sql` converts existing rows (RENTER/OWNER → USER, missing type → PRIVATE) and replaces the role
checks. Wherever the specification says "renter" or "owner", it now means the user's part in a particular booking or
listing, not an account role.

## D-31 Initial categories come from a migration (2026-11-20)

**Concerns:** CLAUDE.md 2.4 (seed data only in dev, never by migrations), FR-4, FR-22. **Decision:** a listing needs a
category and category management arrives only with the admin feature, so `V5__categories.sql` inserts ten categories
(with season months for FR-17) as reference data, not demo data. No demo listings are seeded (author's answer).

## D-32 Catalogue parts that depend on later features (2026-11-20)

**Concerns:** FR-5, FR-7, FR-19. **Decision:** until bookings and reviews exist: the FR-7 date filter applies only
`minDays`/`maxDays` (free units on those dates come with availability); FR-5 "frozen terms while booked" and "delete
only without bookings" are empty checks in `ListingServiceImpl`; owner rating and review count are null/0, sort
`rating` falls back to newest, and the public profile's owner-cancellation and complaint counts are 0.

## D-33 SeaweedFS instead of MinIO for local S3 storage (2026-11-20)

**Concerns:** FR-4a, NFR-13, rule 1.5. **Decision:** MinIO no longer publishes free container images (the Docker Hub
repository is gone and quay.io requires a login), so docker-compose runs SeaweedFS 4.50 (Apache-2.0) with its S3 API on
:8333, identities and anonymous read in `infra/seaweedfs/s3.json`, and `s3-init` creating the bucket. The backend is
unchanged: it speaks plain S3 through the AWS SDK and works with MinIO, AWS S3 or any compatible service via `S3_*`.

## D-34 Leaflet without react-leaflet (2026-11-20)

**Concerns:** CLAUDE.md §3 (Leaflet + react-leaflet), rule 1.5. **Decision:** react-leaflet is licensed Hippocratic-2.1,
which is not OSI-approved, so the unused dependency was removed and maps use Leaflet 1.9.4 (BSD-2-Clause) through a
small hook (`useLeafletMap`) and two components (`MapView`, `PinPicker`).

## D-35 Catalogue details not fixed by the specification (2026-11-20)

**Concerns:** FR-4, FR-4a, FR-5, FR-6. **Decision:** (1) a listing is created first and gets photos afterwards, so it
stays out of the catalogue until it has at least one photo, and its last photo cannot be deleted (replace = upload,
then delete); (2) photo type is decided by the file's magic bytes, not the client's Content-Type; (3) `GET
/equipment/my` (not in the API outline) serves the owner's dashboard with listings in every status; (4) `GET /geocode`
needs a logged-in user, to keep Nominatim traffic to real listing work; (5) a non-public listing's card is visible to its
owner only (404 for others); (6) archiving is one-way, as the specification has no "unarchive".

## D-36 Demo data as a hand-run SQL script (2026-11-20)

**Concerns:** CLAUDE.md 2.4 (demo accounts and ~150 listings in Lviv, never by production migrations), D-21, D-31.
**Decision:** author's request. `infra/seed/demo-data.sql` creates 6 verified owners (`seed.*@example.com`, password
`Password123`; two of them companies) and 150 listings around Lviv and five nearby towns, three of them archived, each
with one or two generated placeholder photos. It is outside `db/migration`, so Flyway never runs it; every insert is
guarded with `NOT EXISTS`, so re-running is harmless. Rows reference users and categories by e-mail and name, never by
id, so identity sequences stay intact. The photo files live in `infra/seed/photos` and are uploaded to
`renthub-photos/seed/` by the `s3-init` job. Street names and coordinates are random within the area, so a pin does not
match its street.

## D-37 Versioned commission rates instead of a settings singleton (2026-11-20)

**Concerns:** FR-10, FR-22, NFR-10; supersedes the settings part of D-9 and D-26. **Decision:** author's request (no
singleton rows; rates may change over time and discounts may come later). `V7` creates `commission_rates (rate,
valid_from)`: the rate in force at a moment is the newest row whose `valid_from` has started; rows are never edited, a
new rate is a new row. The first row copies the 10 % from the old `settings` table, which stays in the database unused
(dropping it needs the author's explicit request, CLAUDE.md 2.4). Every booking stores `commission_rate_id` together
with the computed `commission` and the `price_per_day` it was priced with, so later rate or price changes never alter
existing bookings. Managing rates is part of the admin feature.

## D-38 Booking horizon of 365 days (2026-11-20)

**Concerns:** FR-9, FR-10, NFR-1. The specification has no upper bound. **Decision:** author's approval. A booking's
last day may be at most today + 365 days (`BOOKING_HORIZON_DAYS`), the same limit applies to calendar blocks, and the
availability endpoint answers at most 366 days per request, which keeps the per-day computation bounded.

## D-39 Pending requests hold their units (2026-11-20)

**Concerns:** FR-9 ("NEW requests do not reserve units"), FR-7, FR-11. **Decision:** author's request — a renter
must not be able to request days already requested by someone else. Free units on a day are therefore
`quantity − Σ units of NEW and CONFIRMED bookings` (0 on a blocked day), everywhere: the availability endpoint, the
catalogue date filter, the check on creation and the re-check on confirmation (which ignores the booking being
confirmed). A NEW request releases its units when it is rejected, cancelled or expires (FR-15b, at most 48 h). Both
creation and confirmation lock the listing row first (FR-11), so parallel requests for the last unit get one `201` and
one `409 booking.unavailable` with the days lacking units.

## D-40 Availability blocks and booking details not fixed by the specification (2026-11-20)

**Concerns:** FR-8, FR-12, FR-13, FR-13a, FR-15b, FR-3b. **Decision:** (1) a block may not overlap another block or
days held by a NEW/CONFIRMED booking (`409 block.has_bookings` lists them — the owner rejects or cancels first) and
can only cover today…horizon; creating one takes the listing row lock; (2) confirming a request whose deadline has
passed answers `409 booking.expired` even if the hourly job has not run yet, and only a public listing can be
confirmed; (3) the renter's cancel needs no reason, the owner's needs 10–500 characters; an expired request is
`REJECTED` with `expired = true`; (4) `GET /bookings/incoming` accepts an optional `status` filter, lists are paged by
20; (5) the contact endpoint returns the other party's name and phone (admin access comes with the admin feature);
(6) the jobs run by cron in Europe/Kyiv — completion daily at 00:05, expiry hourly — so a request whose first day
arrives expires within an hour after midnight; (7) e-mail notifications (FR-15a) come with `feature/notifications`,
automatic cancellation on admin block (FR-20) with moderation; (8) deleting a listing (allowed only without bookings)
also deletes its calendar blocks; (9) row locks (`PESSIMISTIC_WRITE`) are always taken in one order — listing, then
users by ascending id, then booking — by booking create (listing, renter, owner), confirm (listing, booking),
reject/cancel and the scheduler jobs (booking), listing edit/archive and block creation (listing) and deactivation
(user), so concurrent decisions cannot overwrite each other and FR-3c/FR-5 checks cannot be bypassed; (10) a NEW
request whose FR-15b deadline has passed stops holding units at once (calendar, catalogue, requests, blocks), and an
owner's rejection after the deadline is recorded as expired.

## D-41 Booking e-mails retried in memory (2026-11-23)

**Concerns:** FR-15a ("failures are logged and retried at most a few times"), FR-15b, NFR-13. **Decision:** author's
choice. Every e-mail (booking and account) goes through `MailDelivery` (`RetryingMailDelivery`): up to
`app.mail.retry.attempts` = 3 attempts, pausing `app.mail.retry.backoff` = 10 s and 60 s between them on the async mail
thread; each failure is logged at WARN and the last at ERROR, with a label (e.g. "booking 42 CONFIRMED") and never the
address. Pending retries live only in memory, so a restart during the ~70 s window drops them; no outbox table. While
an e-mail waits for a retry it occupies one of the two async threads, which is acceptable at this scale. Booking
e-mails are a snapshot event (`BookingNotice`) published inside the booking transaction and sent only after it
commits; a deactivated recipient gets none (FR-3c). Six e-mails: new request → owner (with the answer deadline),
confirmed → renter (with the owner's name and phone, FR-3b), rejected / expired / cancelled by the owner (with the
reason) → renter, cancelled by the renter → owner. Completion (FR-15) sends nothing; admin cancellations (FR-20) will
reuse the same notifier.

## D-42 FR-18 review reminders move to the reviews feature (2026-11-23)

**Concerns:** FR-18 ("both sides get a reminder e-mail at completion"), CLAUDE.md 2.5 backlog item 5. **Decision:**
author's choice. A reminder that points to a review form that does not exist yet would be misleading, so the reminder
is implemented in `feature/reviews` together with the form, using `BookingNotifier` and `MailDelivery`.

## D-43 Reviews: details not fixed by the specification (2026-11-26)

**Concerns:** FR-18, FR-19, FR-7, FR-3c. **Decision:** (1) the comment is required (1–1000 characters), as FR-18 says
"rating 1–5 and a comment"; (2) the rating is stored on the user as `rating_sum` and `review_count` (exact) plus a
derived `rating_avg` (NULL without published reviews), updated when a review is published under the target's row
lock, so cards and the rating sort need no aggregation and the average never drifts; (3) when the second side reviews,
both reviews are published in that transaction; otherwise `ReviewPublicationJob` (hourly) publishes a review once
completion + window has passed; (4) the review window counts from `completedAt`, the reply window from `publishedAt`;
(5) the booking's two parties carry their rating, so owners see the renter's rating on requests (author's choice);
(6) besides the FR-18 reminder at completion, the target is e-mailed when a review about them is published and the
author when the target replies (author's choice); (7) the catalogue `rating` sort orders with an explicit CASE (rated
owners by average, unrated last, ties newest), because Spring's `nullsLast()` is not applied and H2 sorts NULL high;
(8) review headings in the UI avoid putting a name after «про»/«від», which Ukrainian would require to be declined.

## D-44 Recommendations: distance first, own listings excluded (2026-11-26)

**Concerns:** FR-16, FR-17 (ranking "categories currently in season first, then by distance, then by owner
rating"), FR-3. **Decision:** author's choice — distance is the main criterion: recommendations are ordered by
distance ascending, then in-season categories, then owner rating (unrated last), then newest; season therefore only
breaks ties at the same distance and is shown as a «Сезон» badge. "In season" means the current month in Europe/Kyiv
(seasons may wrap over New Year), no look-ahead. The viewer's own listings are excluded. The point is the browser
location only for a signed-in user with geolocation consent (never stored; ignored for guests), else the profile city
geocoded with the existing Nominatim client (cached), else the configured start city (`START_CITY`, `START_CITY_LAT`,
`START_CITY_LON`; Lviv). Cards word the distance by origin: «від вас» for the user's point, «від центру» for a city.

## D-45 Complaints and moderation (2026-11-26)

**Concerns:** FR-20, FR-21, FR-22 (complaints part), NFR-11. **Decision (author's model):** a complaint stores the
complainant, the user it is against, exactly one subject — a booking number or a listing number (DB `CHECK`) — and a
description of 10–1000 characters; status NEW → RESOLVED / REJECTED, decided by an admin. A listing complaint is
against its owner (not one's own listing); a booking complaint comes from one of its sides and is against the other.
Only an active user with a **verified e-mail** may complain (author's choice, against spam); one open (NEW) complaint
per author and subject. A complaint hides nothing by itself. The profile counter counts complaints against the user,
including their listings, except those rejected as unfounded. Blocking a user ends their sessions, hides their
listings and cancels every NEW/CONFIRMED booking starting after today where they are renter or owner; blocking a listing
cancels its future bookings the same way; `cancelledBy = ADMIN`, reason «Заблоковано адміністратором», the other side
is e-mailed; rentals already in progress continue; admin cancellations do not count as the owner's. **Unblocking**
restores nothing that was cancelled, and an unblocked listing always becomes **ACTIVE** (author's choice), even if it
had been archived. Locks: the user's listings → the user → each booking (D-40 order). The admin is never blocked.

## D-46 Admin: commission changes, category deletion, statistics E1/E5 and the listing city (2026-11-26)

**Concerns:** FR-22, FR-23, FR-10, FR-4. **Decisions (author's):**
- **Commission:** a new rate (0–100 %, 2 decimals) is a new `commission_rates` row valid from now (D-37): it applies
  at once to new booking requests only; existing bookings keep their stored rate and commission. No scheduled future
  rates (YAGNI). The admin sees the current rate, its `updatedAt` (the version's `validFrom`) and the latest versions.
- **Categories:** names are unique regardless of case; months 1–12, a season may wrap over New Year. A category
  without listings is deleted directly; one with listings (any status) needs `targetId`: the category row and its
  listings are locked, the listings move to the target and the category is deleted in one transaction; without a
  target the answer is 409 `category.has_listings`.
- **Statistics:** the period is whole days in Europe/Kyiv, both ends included. E1 = listings `ACTIVE` now and created
  before the end of the period (labelled as a current snapshot, there is no status history). E2 = CONFIRMED/COMPLETED
  bookings overlapping the period without a day on which such bookings of the listing hold more units than its
  quantity, as a share of all of them (shown as «—» without bookings). E3 = average rating of reviews published in the
  period. E4 = commission of bookings completed in the period. E5 = distinct `city` values among the E1 listings.
- **Listing city:** new `equipment.city` (V10, NOT NULL). Nominatim is now asked for address details and the listing
  form takes the city, town or village from the result; the owner confirms or corrects it. Existing rows (all seed
  data in Lviv) are set to «Львів» by the migration, and the dev seed script includes the city.

