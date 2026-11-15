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
