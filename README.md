# <center> PokeTeam Builder (Backend)

REST API powering [pokemon-team-builder.com](https://pokemon-team-builder.com)

This is a competitive builder with a full reference Pokédex, a team analysis engine, community
sharing and an administration panel.

- **API:** https://api.pokemon-team-builder.com
- **Live app:** https://pokemon-team-builder.com
- **Frontend repository:** https://github.com/ManusolJ/pokemon-frontend

Java 21 · Spring Boot 4 · PostgreSQL 17 · Docker

---

## Screenshots

| Team builder                                  | Team analysis                              |
| --------------------------------------------- | ------------------------------------------ |
| ![Team builder](docs/screenshots/builder.png) | ![Analysis](docs/screenshots/analysis.png) |

| Team Share                                | Team Storage                                  |
| ----------------------------------------- | --------------------------------------------- |
| ![Team Share](docs/screenshots/share.png) | ![Team Storage](docs/screenshots/storage.png) |

| Admin Panel                                | Seed Process                               |
| ------------------------------------------ | ------------------------------------------ |
| ![Admin Panel](docs/screenshots/admin.png) | ![Seed Process](docs/screenshots/seed.png) |

---

## Why it exists

Competitive Pokémon players work across a fragmented set of tools: one site has a
searchable Pokédex but no builder, another has a builder but no way to publish a team,
and none of them keep a synchronised copy of the official game data. Building a single
team means juggling three tabs and a notes file.

PokéTeam Builder unifies data exploration, team composition and team sharing behind one
API, backed by a local mirror of the official dataset so the app does not depend on a
third-party service being up at request time.

---

## What it does

### Team building

- Teams of up to six Pokémon, each configured with nickname, level, gender, shiny flag,
  ability, nature, held item, Tera type and up to four moves.
- Full EV and IV spreads stored per team member, with derived stats calculated from
  base stats, nature and level.
- Only canonical data is assignable: a species can only be given the moves and abilities
  it actually learns.
- Teams are public or private, shareable by slug, and can be liked by other users.

### Team analysis

- **Offensive coverage:** the team's move pool resolved against all 18 defending types.
- **Defensive coverage:** which types threaten the team and which are resisted,
  aggregated across all members.
- **Role spread:** each member classified into a competitive role derived from its stat
  distribution and moveset.
- **Team stats:** per-stat totals, averages and the team's base stat total.

### Reference data (Pokédex)

Paginated and filterable catalogues, all served from the local database:

| Resource  | Entries | Filters                                                                                                           |
| --------- | ------- | ----------------------------------------------------------------------------------------------------------------- |
| Pokémon   | 1,025   | name, type, generation, baby / mythical / legendary / genderless, pre-evolution, evolution item, base stat ranges |
| Moves     | 919     | name, type, category, power range, accuracy range                                                                 |
| Abilities | 311     | name                                                                                                              |
| Items     | 305     | name, category                                                                                                    |
| Natures   | 25      | stat increased / decreased                                                                                        |

Plus a type-effectiveness matrix that resolves dual-type defending combinations.

### Accounts

- Registration, login, logout, password recovery by e-mail and silent access-token
  renewal.
- Per-IP rate limiting on every authentication endpoint.
- Soft deletion, so removing an account preserves the audit trail.

### Administration (`ADMIN` role only)

- User management: search and filter by role, status and registration date; edit,
  disable or delete accounts.
- **Seed process:** on-demand synchronisation of the entire dataset from
  [PokéAPI](https://pokeapi.co/), behind an explicit destructive-operation confirmation.
- **Seed logs:** every run recorded with trigger, duration, records inserted, error count
  and terminal state. A representative run: 127,633 records in 15m 43s with 0 errors.
- **Audit logs:** a separate trail of administrative actions.

---

## Architecture

```
                        ┌──────────────────────────────┐
   Browser ──HTTPS──►   │  Cloudflare                  │
                        │  · Workers (Angular 21 SPA)  │
                        │  · Sprite hosting            │
                        │  · Tunnel → api.*            │
                        └──────────────┬───────────────┘
                                       │ outbound-only tunnel
                        ┌──────────────▼───────────────┐
                        │  Ubuntu 24.04 server         │
                        │  ┌────────────────────────┐  │
                        │  │ Spring Boot 4 / Java 21│  │
                        │  │ Temurin 25 JRE Alpine  │  │
                        │  └───────────┬────────────┘  │
                        │  ┌───────────▼────────────┐  │
                        │  │ PostgreSQL 17          │  │
                        │  │ Flyway V1–V23          │  │
                        │  └────────────────────────┘  │
                        └──────┬──────────────┬────────┘
                               │              │
                     admin-triggered      SMTP/TLS
                               │              │
                        ┌──────▼─────┐  ┌─────▼─────┐
                        │  PokéAPI   │  │   Brevo   │
                        └────────────┘  └───────────┘
```

Stateless client–server split. No server-side sessions and no server-side rendering;
authentication travels entirely in the `Authorization` header. Both containers share a
private Docker network and the application binds only to the server's loopback
interface, which makes the tunnel the sole ingress.

### Layering

```
controllers/      REST endpoints, no business logic
services/
  ├── query/      read paths
  ├── command/    write paths
  ├── auth/       authentication and token lifecycle
  └── seed/       PokéAPI synchronisation
repositories/     Spring Data JPA
entities/         JPA model
dtos/             front · pokeapi · auth
mappers/          MapStruct
infrastructure/   security · logging interceptors · custom validation ·
                  global exception handling · scheduled tasks
```

Query and command services are kept separate (light CQRS) because the read paths are
heavily cached and paginated while the write paths are transactional and validation-heavy.

---

## Design decisions

### Idempotent seeding keyed on external IDs

The dataset is not bundled with the application. An administrator triggers an import from
PokéAPI, and the obvious risk is that re-importing would orphan every saved team.

Catalogue entities are therefore keyed on **PokéAPI's own stable identifiers** rather than
locally generated surrogate keys. A full re-seed replaces reference data in place while
user-owned rows keep pointing at the same species, moves and items. Refreshing the entire
catalogue is a safe operation.

The endpoint follows a **fire-and-poll** pattern: it writes a `seed_log` row, returns
immediately, and runs the synchronisation on a background thread with pagination and
retries against PokéAPI. Progress and outcome are read back from the log rather than held
open on an HTTP connection since a 15-minute request would time out at every layer in between.

The import is deliberately manual and privileged: reference data changes a few times a
year, so a scheduled job would burn requests against a free public API for nothing.

### Species and forms as separate entities

`PokemonSpecies` models the Pokédex-level entity (Pikachu: evolution chain, egg groups,
flavour text). `Pokemon` models a concrete battle form (Mega Venusaur: its own base
stats, types, sprites and abilities).

Collapsing these into one table it breaks immediately because regional variants and mega evolutions share a species but differ in every stat that
matters competitively. Splitting them keeps the Pokédex browsable by species while the
team builder references exactly the form being used.

### Cloudflare Tunnel instead of port forwarding

The server is a physical machine on a home network. Port forwarding would expose the LAN
directly to the internet and leave certificate renewal and firewall rules as manual work.

The tunnel establishes an **outbound-only** connection to Cloudflare's edge, so no inbound
port is open on the network at all. TLS termination, certificate renewal and DDoS
protection are handled upstream. For a self-hosted deployment this was the strongest
security posture available without renting infrastructure.

### JWT with persisted refresh tokens

The frontend is a separate SPA on a different origin, so stateless authentication avoids
sticky sessions and cross-origin cookie handling.

Access tokens are short-lived; refresh tokens are persisted in the database with a family
identifier and a revocation flag, so a session can be invalidated server-side — which a
pure stateless JWT cannot do. The frontend interceptor renews proactively before
expiry, so the user never sees a failed request mid-session.

This was also the first authentication system I had built. JWT offered the best balance
between a security model I could reason about completely and an implementation I could
get correct, and the persisted refresh layer was added specifically to close the
"tokens cannot be revoked" gap.

### Rate limiting before it was a problem

Authentication endpoints are rate-limited per IP with Bucket4j. A login endpoint with no
throttle is a credential-stuffing target from the day it is public, and this application
runs on hardware I own.

### Caching and pagination

Pokédex reads are cached in memory with Caffeine and every list endpoint paginates and
filters in SQL through a shared filter DTO. With 1,025 species and 919 moves, filtering
in application memory would mean loading the entire catalogue per request.

### Soft deletion

Teams and users are soft-deleted via a `deleted_at` column rather than removed. Hard
deletion would break the audit log's references and make moderation actions
unreviewable after the fact. However, after a time they are completely removed from the system via a weekly scheduled job.

---

## API overview

All endpoints live under `/api`. Full interactive documentation is generated from
annotations by SpringDoc and served at `/swagger-ui.html`.

| Group          | Representative endpoints                                                                                                        | Access                |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------- | --------------------- |
| Authentication | `/auth/register`, `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/password-reset-request`, `/auth/password-reset`        | Public (rate-limited) |
| Pokédex        | `/pokemon`, `/species`, `/moves`, `/abilities`, `/items`, `/types`, `/natures` — each with `filter`, `id`, `summaries`, `count` | Public                |
| Teams          | `/teams/public/filter`, `/teams/public/id`, `/teams/me/filter`, `/teams/me/id`, `/teams` (CRUD), `/teams/{id}/like`             | Mixed                 |
| Users          | `/users`                                                                                                                        | Authenticated         |
| Administration | `/admin/seed`, `/admin/seed-logs/filter`, `/admin/audit-logs/filter`, `/admin/users/{id}/batch`                                 | `ADMIN`               |
| Contact        | `/contact`                                                                                                                      | Public                |

---

## Tech stack

| Layer         | Technology                                               |
| ------------- | -------------------------------------------------------- |
| Language      | Java 21                                                  |
| Framework     | Spring Boot 4 (Web, Data JPA, Security, Mail)            |
| Auth          | JJWT 0.13, persisted refresh tokens, BCrypt              |
| Rate limiting | Bucket4j (per-IP, auth endpoints)                        |
| Cache         | Caffeine                                                 |
| Database      | PostgreSQL 17                                            |
| Migrations    | Flyway (V1–V23)                                          |
| Mapping       | MapStruct, Lombok                                        |
| API docs      | SpringDoc OpenAPI + Swagger UI                           |
| Mail          | Spring Mail over Brevo SMTP                              |
| Build         | Maven (wrapper included)                                 |
| Runtime       | Multi-stage Docker build → Eclipse Temurin 25 JRE Alpine |
| CI/CD         | GitHub Actions → SSH deploy to self-hosted server        |
| Ingress       | Cloudflare Tunnel                                        |

---

## Running it locally

**Requirements:** Docker and Docker Compose. Nothing else — the JDK and PostgreSQL both
run in containers.

```bash
git clone https://github.com/ManusolJ/pokemon-backend.git
cd pokemon-backend

cp .env.example .env.dev
# fill in the values — see the table below

docker compose -f docker-compose.dev.yml up --build
```

Flyway applies all migrations automatically on startup. The API is then available at
`http://localhost:${PORT}`, with Swagger UI at `/swagger-ui.html`.

### First-run setup

The schema ships with no administrator and empty catalogue tables. Two manual steps:

1. **Create an admin.** Register through the normal public flow, then promote the account
   directly in the database:
   ```sql
   UPDATE app_user SET role = 'ADMIN' WHERE username = '<your-username>';
   ```
2. **Seed the catalogue.** Sign in as that admin and trigger the import from the admin
   panel, or `POST /api/admin/seed`. A full run takes roughly 15 minutes and makes several
   thousand requests to PokéAPI — run it once and keep the volume.

### Environment variables

`.env.example` documents every supported variable. Development reads `.env.dev`,
production reads `.env.prod`; neither is committed.

| Group                | Variables                                                                                                                                       |
| -------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------- |
| Database (container) | `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`                                                                                             |
| Application          | `PORT`, `SPRING_PROFILES_ACTIVE`, `CORS_ALLOWED_ORIGIN`                                                                                         |
| Database (client)    | `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`                                                                                       |
| Mail                 | `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`, `CONTACT_TO`                                                           |
| Security             | `JWT_SECRET`, `ACCESS_TOKEN_EXPIRATION_MS`, `REFRESH_TOKEN_EXPIRATION_MS`, `PASSWORD_RESET_TOKEN_EXPIRATION_MINUTES`, `PASSWORD_RESET_BASE_URL` |

---

## Deployment

Deployment is automated. Pushing to `main` is the entire operational procedure.

**Verification stage** - runs on every push and pull request to `main`: compiles the
project and confirms the Docker image builds. Acts as a quality gate and blocks the rest
of the pipeline on failure.

**Deploy stage** - runs only after a successful verification on `main`. The runner
connects over SSH to the production server and executes a deployment script versioned in
this repository, which rebuilds and restarts the Compose stack, then **polls the health
endpoint for one minute**. If the application reports healthy the deploy is accepted;
otherwise the script dumps a recent log excerpt and exits non-zero, so the failure is
diagnosable from the Actions run itself rather than by SSHing in afterwards.

The production stack is PostgreSQL 17 with a persistent volume plus the application
container, on a private Docker network, with the application bound to loopback only and
published exclusively through the Cloudflare Tunnel.

---

## Project status and roadmap

Live and in use, but not finished. Known gaps in the order I intend to close them:

- [ ] **Automated tests.** The highest-value targets are the stat calculator and the
      type-effectiveness resolver — both are pure functions with known expected outputs.
      Planned: JUnit for unit tests, Testcontainers for the repository layer.
- [ ] **Observability.** Metrics and traces; right now failure diagnosis is log-based.
- [ ] **Cancellable seed runs.** The import cannot be interrupted once started.
- [ ] **Staging environment** in the pipeline, with manual promotion to production.
- [ ] **Team comparator** — pit two public teams against each other and analyse the
      matchup.
- [ ] **Showdown import/export** in the standard team format.
- [ ] **Admin statistics dashboard** — teams created per day, most-used Pokémon, like
      rates.

---

## Disclaimer

Pokémon and all related names are trademarks of Nintendo, Game Freak and The Pokémon
Company. This is a non-commercial fan project built for learning purposes and is not
affiliated with or endorsed by them. Game data comes from the community-maintained
[PokéAPI](https://pokeapi.co/).

---

## Author

**Manuel Soler Juan** - Junior full stack developer
[GitHub](https://github.com/ManusolJ) · [LinkedIn](https://linkedin.com/in/manusolerj)
