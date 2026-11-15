# RentHub — Requirements Specification

This document defines **what** RentHub must do: the domain, functional and non-functional requirements, actors and permissions, and the API outline. **How** to build it (engineering rules, stack, git workflow) is defined in `CLAUDE.md`.

Requirement IDs (FR-n, NFR-n) are stable — reference them in commit messages, PR descriptions and `docs/DECISIONS.md`. When a requirement changes, edit this file in the same branch as the code.

---

## 1. Domain

**RentHub** is a web marketplace where equipment owners (companies and private individuals) publish equipment for rent and renters book it for specific dates. Segments: construction tools, photo/video gear, specialised equipment. The platform earns a commission on each rental; payments between users happen outside the platform. Start city: Lviv; the model must support more cities without code changes.

**What makes it different (must be implemented):**
1. Automatic availability check — double booking is impossible.
2. Narrow specialisation — categories with a seasonal demand period.
3. Proactive recommendations — equipment nearby (geolocation by consent), seasonality-aware.
4. Two-sided ratings — renter and owner rate each other after a completed rental.

**Domain concepts** (conceptual model of what the system stores and reasons about — fields and names are indicative, not a mandatory class design)

| Entity | Fields | Notes |
|---|---|---|
| User | id, name, email (unique), emailVerified, phone, passwordHash, role, city, geoConsent, blocked, deactivatedAt, ownerType? (`PRIVATE`/`COMPANY`), companyName?, edrpou? | roles: `RENTER`, `OWNER`, `ADMIN`; owner type fields apply to owners only (FR-1a); `deactivatedAt` set = account deactivated by the user (FR-3c) |
| Category | id, name (unique), seasonFrom, seasonTo (months 1–12) | a season may wrap over New Year (e.g. 11→2) |
| Equipment | id, owner, category, title, description, condition, pricePerDay, quantity (≥ 1), minDays, maxDays?, pickupAvailable, deliveryAvailable, deliveryNote?, address, lat, lon, status | status: `ACTIVE`, `ARCHIVED`, `BLOCKED` |
| EquipmentPhoto | id, equipment, storageKey, position | 1–5 photos per listing, stored in S3-compatible object storage |
| EmailToken | id, user, type (`VERIFY_EMAIL`, `RESET_PASSWORD`), tokenHash, expiresAt, usedAt | single-use; store only a hash of the token |
| AvailabilityBlock | id, equipment, dateFrom, dateTo, reason | period the owner closed for rental |
| Booking | id, equipment, renter, units (≥ 1), dateFrom, dateTo, handover (`PICKUP`/`DELIVERY`), totalPrice, commission, status, createdAt, decidedAt?, cancelledBy? (`RENTER`/`OWNER`/`ADMIN`), cancelReason? | status: `NEW`, `CONFIRMED`, `REJECTED`, `CANCELLED`, `COMPLETED` |
| Review | id, booking, author, target, rating (1–5), comment, createdAt, publishedAt?, reply?, repliedAt? | one per side per booking; hidden until published (FR-18) |
| Complaint | id, author, targetUser?, targetEquipment?, text, status, createdAt | status: `NEW`, `RESOLVED`, `REJECTED`; at least one target |
| Settings | feeRate (%), updatedAt | singleton row (id = 1) |

Conventions: a rental period is `[dateFrom, dateTo]` with **`dateTo` inclusive**. Prices are in UAH with 2 decimals; "today" means the current date in the Europe/Kyiv time zone.

---

## 2. Functional requirements

### Accounts
- **FR-1** Register with name, email, phone, password, city and role (renter or owner); email must be unique. A verification e-mail with a single-use link (valid 24 h) is sent; an unverified user may log in and browse but cannot create listings or booking requests until the e-mail is verified. Provide "resend verification".
- **FR-1a** An owner specifies the owner type: private person or company. For a company, the company name and ЄДРПОУ code (exactly 8 digits) are required. Listing cards and the owner profile show a "Компанія" / "Приватна особа" label with the company name; the catalogue can be filtered by owner type. The type is editable in the profile.
- **FR-2** Log in with email and password; access to features depends on the role. A blocked user cannot log in. Nobody can register as `ADMIN`.
- **FR-2a** The first administrator is created at application start-up from environment variables (`ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME`) if no `ADMIN` exists yet; the account is created already verified. Without these variables no admin is created (log a warning). The password is never logged.
- **FR-3** Give and withdraw consent to process geolocation. Withdrawal immediately disables geo features.
- **FR-3a** Password reset: the user requests a reset by e-mail; a single-use link (valid 1 h) lets them set a new password. The response never reveals whether the e-mail exists; old sessions are invalidated after a reset.
- **FR-3b** Phone numbers are private: a user's phone is visible **only to the other party of a `CONFIRMED` or `COMPLETED` booking** (so they can arrange the handover) and to admins. Never expose it in the catalogue, equipment cards, reviews or lists.
- **FR-3c** Account deactivation: a user can deactivate their own account (password confirmation required). It is **forbidden while the user has active bookings** — as renter or as owner, any `NEW` or `CONFIRMED` booking whose `dateTo ≥ today`; the response lists them. On deactivation: sessions are invalidated, the user's listings are hidden from the catalogue and recommendations (their status is kept), the profile and reviews show the user as "deactivated", no e-mails are sent to them. Data is retained. **Restoration:** logging in with correct credentials to a deactivated account offers "Restore account"; after confirmation `deactivatedAt` is cleared and listings reappear. A blocked (by admin) account cannot be restored by the user.

### Catalogue
- **FR-4** An owner creates a listing: title, description, category, condition, price per day, 1–5 photos and an address. The backend **geocodes the address** (OpenStreetMap Nominatim) into coordinates; the owner sees the point on a map and may correct it by moving the pin. If geocoding fails, the owner must place the pin manually.
- **FR-4a** Photo upload: JPEG/PNG/WebP, ≤ 5 MB each, validated by content type and size on the server, stored in **S3-compatible object storage** (the database keeps only the storage keys), served via public/pre-signed URLs; replacing or deleting a photo removes the stored object. Image size is limited to protect the storage.
- **FR-4b** Rental terms per listing: minimum rental period `minDays` (default 1) and optional maximum `maxDays` (`1 ≤ minDays ≤ maxDays`); handover options — pickup and/or delivery (at least one must be enabled); for delivery an optional free-text note about terms/cost (e.g. "delivery within Lviv, cost by agreement"). Delivery cost is not calculated by the platform and is not part of `totalPrice`.
- **FR-4c** A listing has a **quantity** of identical units (default 1), e.g. a rental point with 5 identical drills. A booking request is for a number of `units` (1…quantity).
- **FR-5** An owner edits, archives and deletes own listings. A listing with active (`NEW`/`CONFIRMED`, future) bookings can only be archived; deletion is allowed only if the listing has no bookings at all. **While the listing has active bookings** (`NEW`, or `CONFIRMED` with `dateTo ≥ today`) its **price and rental terms cannot be edited** — `pricePerDay`, decreasing `quantity` (increasing is always allowed), `minDays`, `maxDays`, pickup/delivery options and the delivery note (`409` with an explanation listing the active bookings); descriptive fields (title, description, condition, photos) remain editable.
- **FR-6** Anyone (including guests) can browse the catalogue and open an equipment card (with owner rating and reviews). Blocked and archived listings are not shown.
- **FR-7** Search and filter by keywords, category, price range, distance, rental dates (only equipment available for the given dates, also respecting `minDays`/`maxDays`) handover options ("pickup available", "delivery available") and owner type (private/company).
  - **Price filter** works in two modes: by price per day per unit (always), and — when rental dates (and optionally `units`, default 1) are given — by **total cost** for that period (`days × units × pricePerDay`, min/max); in that mode each card also shows the calculated total.
  - **Sorting:** price (ascending/descending; by total cost when dates are given, otherwise by price per day), distance (ascending; only when the user's location is known, otherwise this option is disabled), owner rating (descending; users without published reviews last). Default sort: distance if location is known, otherwise price ascending. Ties are broken by newest listing first. Pagination with a fixed page size (default 20).

### Availability and booking
- **FR-8** An owner maintains an availability calendar: block and unblock date ranges.
- **FR-9** The system checks availability of `units` for a date range **day by day**: for every day `d` in `[dateFrom, dateTo]`, `units + Σ units of CONFIRMED bookings of that equipment covering d ≤ quantity`. A day covered by an availability block is unavailable for all units (a block closes the whole listing). Overlap rule for intervals: `a.from ≤ b.to && b.from ≤ a.to`. `NEW` requests do not reserve units. The availability endpoint returns, per day in the requested window, the number of free units.
- **FR-10** A renter creates a booking request. The system calculates `days = dateTo − dateFrom + 1`, `totalPrice = days × units × pricePerDay`, `commission = totalPrice × feeRate / 100` (HALF_UP, 2 decimals).
- **FR-11** Overbooking is forbidden (never more confirmed units on any day than `quantity`). Availability is checked when the request is created and **again** when the owner confirms; the confirm (and create) check runs inside one transaction that **first locks the equipment row** (`SELECT … FOR UPDATE` / pessimistic write lock), so concurrent confirmations for the same equipment are serialized on every database. A conflict returns `409` with the days that lack free units.
- **FR-12** The owner confirms or rejects a request.
- **FR-13** A renter views own bookings and may cancel a booking (`NEW` or `CONFIRMED`) only before the rental starts (`today < dateFrom`).
- **FR-13a** An owner may cancel a `CONFIRMED` booking only before the rental starts and **must give a reason** (10–500 chars); the reason is shown to the renter and included in the e-mail. The owner's profile shows the number of bookings cancelled by the owner (all time).
- **FR-14** Booking statuses and the only allowed transitions:
  - `NEW → CONFIRMED` (owner), `NEW → REJECTED` (owner);
  - `NEW | CONFIRMED → CANCELLED` (renter, before start);
  - `CONFIRMED → CANCELLED` (owner, before start, with a mandatory reason — FR-13a);
  - `NEW | CONFIRMED → CANCELLED` (automatic on admin block, future bookings only — FR-20);
  - `NEW → REJECTED` (automatic, expiry — FR-15b);
  - `CONFIRMED → COMPLETED` (automatic).
  Any other transition is rejected (`409`/`422`). Keep the transition rules in one cohesive place in the domain, not scattered across services or controllers.
- **FR-15** A daily scheduled job moves `CONFIRMED` bookings with `dateTo < today` to `COMPLETED`.
- **FR-15a** E-mail notifications (sent asynchronously, never blocking or failing the request): to the owner on a new request; to the renter when the request is confirmed (include the owner's contact) or rejected; to the other party on cancellation. Texts are Ukrainian. Failures are logged and retried at most a few times.
- **FR-15b** Request expiry: an hourly scheduled job moves `NEW` requests to `REJECTED` (reason "expired") when **48 hours have passed since `createdAt`, or `dateFrom` has arrived — whichever comes first**; the renter is notified by e-mail. The 48 h value is configurable. The owner sees this deadline on each incoming request in the dashboard.

Booking validation: only future dates (`dateFrom` no earlier than tomorrow — configurable), `dateTo ≥ dateFrom`, number of days within the listing's `minDays`…`maxDays`; the chosen handover mode must be offered by the listing; only `ACTIVE` equipment; an owner cannot book their own equipment.

### Recommendations
- **FR-16** Determine the user's location: browser coordinates if consent is given (**never persisted**, passed per request only), otherwise the profile city; guests get the start city.
- **FR-17** Recommend active equipment within a radius (default 25 km, haversine distance). Ranking: categories currently in season first, then by distance ascending, then by owner rating. Default limit 12. Shown on the home page.

### Ratings and reviews
- **FR-18** After a `COMPLETED` booking, each side (renter and owner) may leave one review for the other: rating 1–5 and a comment (≤ 1000 chars). Rules:
  - **Window:** reviews can be left within **14 days** after the booking became `COMPLETED` (configurable); both sides get a reminder e-mail at completion.
  - **Blind reviews:** a review stays hidden (from the target and the public) until **both** sides have submitted, or the window has expired — then it is published (`publishedAt` set). A scheduled job publishes reviews whose window expired. Until published, the author sees their own review; it cannot be edited after submission.
  - **Reply:** the target may publish **one** public reply (≤ 1000 chars) to a published review, within **14 days** after the review was published (configurable); replies cannot be edited.
  - Reviews are per booking, not per unit: a booking for several units still gets one review per side.
- **FR-19** Show a user's average rating and review count (computed from **published** reviews where the user is the target). The public profile also shows the owner-cancellation count (FR-13a) and the complaint count (FR-21).

### Administration
- **FR-20** Moderate: block/unblock users and listings; blocked listings disappear from the catalogue. On blocking, **future bookings are cancelled automatically**: every `NEW` or `CONFIRMED` booking with `dateFrom > today` — of the blocked listing, or where the blocked user is renter or owner — becomes `CANCELLED` (`cancelledBy = ADMIN`, reason "Заблоковано адміністратором"), and the other party is e-mailed. Bookings already in progress (`dateFrom ≤ today ≤ dateTo`) continue and complete normally. Admin-cancellations do not count towards the owner-cancellation counter. Unblocking does not restore cancelled bookings.
- **FR-21** Any authenticated user can file a complaint about a listing and/or a user (one open complaint per author per target). Complaints **do not hide** a listing automatically — only an admin block does. The user's public profile shows a **complaint counter**: complaints against the user and their listings, excluding those the admin rejected as unfounded.
- **FR-22** Admin resolves or rejects complaints; manages categories (CRUD with season) and the commission rate.
- **FR-23** Admin statistics for a chosen period:
  - E1 number of active listings;
  - E2 share of conflict-free bookings (monitoring query: `CONFIRMED`/`COMPLETED` bookings for which no covered day has more confirmed units than the listing's quantity ÷ all such bookings; expected 100 %);
  - E3 average rating;
  - E4 total commission of `COMPLETED` bookings;
  - E5 number of distinct cities among active listings.

---

## 3. Non-functional requirements

| ID | Area | Requirement |
|---|---|---|
| NFR-1 | Performance | Catalogue and search respond in ≤ 3 s; the availability check in ≤ 1 s with 50 concurrent users. Add DB indexes accordingly. |
| NFR-2 | Reliability | Availability ≥ 99 % per month; daily database backup; documented restore procedure; health endpoint. |
| NFR-3 | Data integrity | ≥ 98 % conflict-free bookings (target 100 %). Guaranteed by construction (transaction + equipment row lock + re-check): parallel confirmations that together would exceed `quantity` on some day cannot all succeed — the excess ones get `409`. DB constraints still guard `units ≥ 1`, `quantity ≥ 1`, `dateTo ≥ dateFrom`. |
| NFR-4 | Security | HTTPS only in production; BCrypt password hashing; role-based access; ownership checks in the service layer; CSRF protection; `passwordHash` never leaves the backend; no secrets in git. |
| NFR-5 | Privacy | Collect only name, email, phone, city. Phone visible only as described in FR-3b. Geolocation only with explicit consent; user coordinates are never stored; privacy-policy page. No payment data. Verification/reset tokens are random, hashed in the DB, single-use and expire. |
| NFR-6 | Usability | A booking takes at most 3 steps; Ukrainian UI with clear validation messages; loading/empty/error states for every request. |
| NFR-7 | Portability | Responsive layout for desktop and mobile; current Chrome, Firefox, Edge, Safari. |
| NFR-8 | Maintainability | Layered code (controller → service → repository); DTOs only in the API; business rules isolated in `service`/`domain` classes. Code structure rules for the frontend are defined in `CLAUDE.md`. |
| NFR-9 | Flexibility | H2 → PostgreSQL without business-logic changes; new categories and cities are data, not code. |
| NFR-10 | Business rules | Future dates only, minimum one day; a review only after `COMPLETED` and once per side; commission = percentage of rental cost; payments outside the platform. |
| NFR-11 | Access | The admin area is accessible to `ADMIN` only. |
| NFR-12 | Scope | No online payments, chat, mobile apps, multi-language UI or ML recommendations in the MVP. |
| NFR-13 | Integrations | Object storage, geocoding and SMTP are configured via environment variables; Nominatim usage must respect its policy (identifying `User-Agent`, ≤ 1 request/second, cache results, no bulk geocoding). In `dev`, use MinIO and Mailpit from Docker Compose. |
| NFR-14 | Abuse protection | Rate limits (per IP and per e-mail): login — 5 failed attempts per 15 min, then a 15-min lock with a Ukrainian message (`429`); registration — 5 per hour per IP; password reset and resend-verification — 3 per hour per e-mail. Limits are configurable; in-memory implementation is enough for a single instance. Responses never reveal whether an e-mail exists. |

Error format: RFC 9457 `ProblemDetail` (`application/problem+json`) with a machine-readable `code`; all input validated with Bean Validation (Ukrainian messages).

---

## 4. Actors and permissions

| Actor | Can |
|---|---|
| **Guest** (unauthenticated) | Browse the catalogue and equipment cards, search/filter, check availability, see recommendations for the start city, register, log in. |
| **Renter** (`RENTER`) | Everything a guest can, plus: create booking requests, view and cancel own bookings (before start), get location-based recommendations, leave reviews after completed rentals, view ratings, file complaints, manage own profile and geolocation consent. |
| **Owner** (`OWNER`) | Everything a renter can (except booking own equipment), plus: create/edit/archive/delete own listings, manage the availability calendar, confirm or reject incoming requests, review renters. |
| **Admin** (`ADMIN`) | Moderate listings and users (block/unblock), handle complaints, manage categories and the commission rate, view statistics. Access to the admin area is exclusive to this role. |
| **Scheduler** (system) | Automatically completes finished bookings (FR-15), expires unanswered requests (FR-15b) and publishes reviews whose window expired (FR-18). No UI. |

Ownership rules (enforced in the service layer, not only in the UI): only a listing's owner may edit/archive it and manage its calendar; only the equipment owner may confirm/reject its requests; only the booking's renter may cancel it; only booking participants may review it.

### Main screens

 Home (search, categories, "Recommendations nearby"), Catalogue (filters), Equipment card (photo gallery, map with the pin, calendar, booking form with instant price and conflict message, reviews, "Report"), Register/Login/Profile (phone, city, consent, deactivate account), Verify e-mail, Forgot/Reset password, My bookings (with the other party's phone after confirmation), Owner dashboard (listings with photo upload and a map pin editor, calendar, incoming requests), Admin (moderation, complaints, categories, commission, statistics).

## 5. API outline (`/api/v1`, JSON, ISO-8601 dates)

| Endpoint | Purpose | Access |
|---|---|---|
| `POST /auth/register`, `/auth/login`, `/auth/logout`; `GET /auth/me` | accounts | guest / all |
| `POST /auth/verify-email`, `/auth/resend-verification`, `/auth/forgot-password`, `/auth/reset-password` | e-mail verification, password reset | guest / authenticated |
| `PUT /users/me/geo-consent` | consent | authenticated |
| `POST /users/me/deactivate`, `POST /auth/restore` | deactivate / restore own account | authenticated / deactivated user with valid credentials |
| `GET /categories` | categories | guest |
| `GET /equipment`, `GET /equipment/{id}` | catalogue (filters: `q`, `categoryId`, `priceMin`/`priceMax`, `dateFrom`/`dateTo`, `units`, `lat`/`lon`/`radiusKm`, `pickup`, `delivery`, `ownerType`; `sort`: `price,asc\|desc`, `distance`, `rating`; `page`), card | guest |
| `POST/PUT/DELETE /equipment[/{id}]`, `POST /equipment/{id}/archive` | manage listings | owner (own) |
| `POST /equipment/{id}/photos`, `DELETE /equipment/{id}/photos/{photoId}` | photo upload / removal | owner (own) |
| `GET /geocode?q=` | address → coordinates preview for the map pin | owner |
| `GET /equipment/{id}/availability?from&to` | availability + occupied ranges | guest |
| `GET/POST/DELETE /equipment/{id}/blocks` | calendar | owner |
| `POST /bookings`; `GET /bookings/my`, `/bookings/incoming` | requests, lists | authenticated |
| `POST /bookings/{id}/confirm`, `/reject`, `/cancel` | decisions, cancellation (owner must send `reason`) | owner / renter |
| `GET /users/{id}` | public profile: name, city, rating, review count, owner cancellations, complaint count | all |
| `GET /bookings/{id}/contact` | the other party's name and phone (only `CONFIRMED`/`COMPLETED`) | booking participants |
| `GET /recommendations?lat&lon` | recommendations | guest / authenticated |
| `POST /bookings/{id}/reviews`; `GET /users/{id}/reviews` (published only) | reviews, rating | participant / all |
| `POST /reviews/{id}/reply` | reply to a published review | review target |
| `POST /complaints` | complaint | authenticated |
| `/admin/complaints`, `/admin/users`, `/admin/equipment`, `/admin/categories`, `/admin/settings`, `/admin/statistics` | administration | ADMIN |
