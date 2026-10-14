# TasteTribe — Recipe Sharing Platform

## What it is
A full-stack recipe sharing website. Cooks publish structured recipes, discover and
filter others' dishes, rate and review them, follow each other, scale any recipe to a
new serving count, build a persistent shopping list, and use an AI sous-chef for
cooking Q&A, pantry-based recipe generation and ingredient substitutions.

## Stack (Java backend, as required)
- **Backend**: Java 17 + Spring Boot 3.3 + Spring MVC REST + **JDBC** (`NamedParameterJdbcTemplate`
  + HikariCP). Maven project at `/app/java-backend`. Serves everything under `/api` on **port 8001**.
- **Database**: **MySQL/MariaDB** (`tastetribe` schema, user `tribe`/`tribe123`),
  normalized DDL in `src/main/resources/schema.sql`, applied at boot (`spring.sql.init.mode=always`).
- **Frontend**: Vite + React 19 + TypeScript strict + Tailwind v4 + shadcn/ui, port 3000,
  proxies `/api/*` → `localhost:8001`.
- **Supervisor programs**: `java-backend`, `mysql`, `frontend`. The template's Python
  `backend` program is stopped and unused.
- **AI**: Emergent LLM key (OpenAI-compatible gateway) called server-side only from
  `LlmClient`; the key never reaches the browser.

## Layered architecture
`Controller → Service → DAO (interface) → DaoImpl (JDBC) → MySQL`

Rubric-relevant highlights:
- **Inheritance**: `BaseEntity` → `User`/`Recipe`/`Review`/`Comment`/`Category`/`Report`;
  `AppException` → `NotFoundException`, `ForbiddenException`, `UnauthorizedException`,
  `DuplicateResourceException`, `BadRequestException`.
- **Interfaces + polymorphism**: `GenericDao<T, ID>`, `UserRecipeActionDao` implemented by
  both `LikeDao` and `FavoriteDao` over one shared `AbstractUserRecipeDaoImpl` (template method).
- **Generics**: `GenericDao<T, ID>`, `Map<String, User>` batch lookups, Jackson `TypeReference`.
- **Collections & Streams**: trending/recommended scoring, profile rating averages, DTO mapping.
- **Enums**: `Role`, `Difficulty`, `RecipeStatus`, `ReportTargetType` (with Jackson `@JsonValue`).
- **Exception handling**: `GlobalExceptionHandler` (`@RestControllerAdvice`) returns a uniform
  `{"detail": ...}` and never leaks stack traces.
- **Multithreading & synchronization**: `ViewTrackerService` buffers view counts in a
  `ConcurrentHashMap<String, LongAdder>` and a daemon `ScheduledExecutorService` flushes
  batched UPDATEs every 5 s via a `synchronized` drain.
- **Servlets & web integration**: `AuthFilter` (jakarta `Filter`) resolves the session cookie
  into a request attribute; `SessionContext` exposes `current/require/requireAdmin`.
- **DTOs + validation**: Java records in `dto/` with Jakarta Bean Validation (`@Valid`).
- **Security**: BCrypt password hashing, httpOnly cookie sessions in the `sessions` table,
  owner/admin authorization on edit+delete, all SQL fully parameterized (no injection surface).

## Data model (MySQL tables)
`users`, `sessions`, `password_resets`, `categories`, `recipes`,
`recipe_ingredients` (normalized child: name/quantity/unit/is_optional/position),
`reviews` (unique on recipe_id+user_id), `comments`, `likes`, `favorites`, `follows`,
`recently_viewed`, `shopping_list_items`, `reports`, `ai_messages`,
`meal_plan_entries` (user + ISO date + slot + servings).

## Added features (second iteration)
- **Cover photo upload** — `POST /api/uploads/image` (multipart, login required, 8 MB cap,
  image-only whitelist, UUID filename so the client name is never trusted). Files land in
  `java-backend/uploads` and are served back at `/api/uploads/**` via a Spring resource
  handler. `CoverPhotoPicker` offers Upload (drag-drop or file dialog) or URL.
- **Cook Mode** — `CookMode.tsx`, a full-screen dark stepper opened by "Start cooking".
  One step in large type, scaled ingredients beside it, keyboard arrows/Esc, and a
  countdown **timer auto-seeded from any duration mentioned in the step text**
  (e.g. "simmer 12 minutes" → 12:00) with start/pause/reset.
- **Report buttons** — `ReportDialog.tsx` on every recipe (under Method) and every comment
  (compact). Posts to the existing `POST /api/reports`; rows appear in `/admin?tab=reports`
  and Dismiss removes them.
- **Weekly meal planner** — `/meal-planner`. 7-day × 3-slot grid; drag saved recipes from
  the side pool onto any cell, drag planted entries between cells (`PATCH /meal-plan/entries/{id}`),
  or use the `+` picker on touch. **Build shopping list** (`POST /meal-plan/shopping-list?weekOf=`)
  aggregates every planned recipe, scales each to its planned servings and merges identical
  name+unit lines into one list. Weeks are normalised to Monday server-side.

## Key flows
1. **Register/Login** → httpOnly `tt_session` cookie; `GET /api/auth/me` returns user or null.
2. **Discover** `/recipes` → filter by q/ingredient/cuisine/category/difficulty/dietary/tag/maxTime,
   sort by newest/rating/popular/time, paginated.
3. **Recipe detail** → serving scaler calls `GET /api/recipes/{id}/scale?servings=N`
   (all arithmetic in Java), ingredient checklist, AI substitution per ingredient,
   add-all-to-shopping-list, 1-5 star review (one per user, updatable), comments.
4. **Publish** `/recipes/new` → dynamic ingredient rows, steps, nutrition, tags,
   publish or save as draft. Optional AI pantry generation pre-fills the form.
5. **Dashboard** → stats + My recipes / Saved / Drafts tabs.
6. **Feed** → Latest / Trending / People you follow / Recommended.
7. **Shopping list** → persisted per user, merges duplicate ingredients, check/remove/clear.
8. **Admin** `/admin` → stats, users, recipes, comments, reports moderation (403 for non-admins).
9. **Meal planner** `/meal-planner` → drag saved recipes onto a week grid, then build one
   combined shopping list for the whole week.
10. **Cook mode** → "Start cooking" on any recipe opens the full-screen step view with timers.

## Roles
- `USER` — publish/edit/delete own recipes, rate, comment, follow, shop.
- `ADMIN` — all of the above plus the admin panel, category CRUD and deleting any
  recipe/comment/user.

## Seed data (idempotent, `DataSeeder`, runs only when `users` is empty)
- 8 categories, 6 users (5 cooks + 1 admin), 12 published recipes with real food photography,
  2-4 reviews each (ratings 4-5), 2 comments each, random likes/favorites/follows.

## Build & run
```bash
cd /app/java-backend && mvn -o package -DskipTests   # build jar
sudo supervisorctl restart java-backend              # run (port 8001)
```
Frontend hot-reloads via Vite. A `mvn package` + `supervisorctl restart java-backend`
is required after any Java change (no hot reload on the backend).

## Profile picture upload (device library)
- `AvatarPicker` (frontend/src/components/profile/AvatarPicker.tsx): upload from device (POST /api/uploads/image multipart) or paste a URL; 8 MB / image-type guard.
- Used on signup (pages/Auth.tsx) and in `EditProfileDialog` (Dashboard header) which saves name/bio/avatarUrl via PATCH /api/auth/profile.
