# AGENTS.md

## Agent quick start (60 seconds)
- Use Node 24.x assumptions; start with `npm install` then `npm run dev` (Vite on `http://localhost:8090`, proxies to backend `:8091`).
- Before handing off changes, run: `npm run check:format`, `npm run check:lint`, `npm run build`, `npm run test:coverage`.
- Follow layer direction strictly: `src/ui` -> `src/application` (use cases/services/ports) -> `src/domain`; transport/storage code stays in `src/adapter`.
- Keep auth/fetch/cookie details in REST adapters (for example `src/adapter/rest/*.ts`, CSRF via `src/adapter/util/Csrf.ts`).
- Add new dependencies through `useX()` factories and `reset...()` hooks in layer `index.ts` files (`src/adapter`, `src/application/services`, `src/application/usecases`, `src/domain/services`).
- Routes are discovered from `src/ui/views/**/Route.ts`; each route module exports one default `RouteRecordRaw`.
- Guard behavior comes from route `meta` (`authenticated`, `permissions`) in `src/ui/plugins/router.ts`; failed auth stores `login-redirect` in `localStorage`.
- Tests use Vitest + happy-dom + fake IndexedDB; MSW is strict (`onUnhandledRequest: 'error'`) so every network call must be mocked.
- i18n is strict in tests (`throwOnMissing: true`), so missing locale keys fail tests immediately.

## Scope and precedence
- This file applies to `frontend/` only.
- Prefer `frontend/.github/copilot-instructions.md` and active config files over `README.md` when they disagree.
- Known drift: `README.md` still mentions Node 22 / ports 8080+8081, while current config uses Node 24.x and Vite on 8090 proxied to 8091 (`vite.config.ts`).

## Fast start (verified from repo config)
- Install and run: `npm install`, `npm run dev`.
- Quality gates used in CI-like flow: `npm run check:format`, `npm run check:lint`, `npm run build`, `npm run test:coverage`.
- Tests run with `TZ=CET` and Vitest (`package.json`).

## Architecture map (follow this layering)
- UI (`src/ui`) calls application use cases/services (`src/application`).
- Application depends on ports (`src/application/ports`) and domain services/entities (`src/domain`).
- Adapters (`src/adapter`) implement ports (REST + storage) and are the only place for transport/storage details.
- Keep fetch/auth/cookie handling in adapters (example: `src/adapter/rest/EventRestRepository.ts`, `src/adapter/util/Csrf.ts`).

## Dependency wiring pattern (important)
- Use `useX()` singleton factories from `index.ts` files for DI-style wiring:
  - `src/adapter/index.ts`
  - `src/application/services/index.ts`
  - `src/application/usecases/index.ts`
  - `src/domain/services/index.ts`
- Each layer has `reset...()` functions; tests rely on them being called after each test (`test/vitest.setup.ts`).
- When adding a new repository/service/use case, wire it in the matching `index.ts` and reset function.

## Routing and auth conventions
- Routes are auto-discovered from `src/ui/views/**/Route.ts` (`import.meta.glob` in `src/ui/plugins/router.ts`).
- Route guards read `meta` from `RouteMetaData` (`authenticated`, `permissions`) and call `AuthUseCase.authenticate(true)`.
- Login redirect flow uses `localStorage` key `login-redirect` in router guard.
- Route modules should export one default `RouteRecordRaw` (see `src/ui/views/home/Route.ts`, `src/ui/views/events/Route.ts`).

## Data, caching, and offline behavior
- REST calls typically use `credentials: 'include'`; mutating calls include `X-XSRF-TOKEN` from cookie helper.
- Caching services hydrate local storage first and tolerate network failures (example: `EventCachingService` prefetches +/-2 years and falls back to cache).
- Storage backend is env-switched via `VITE_USE_INDEXED_DB` (`IndexedDBStorage` vs `InMemoryStorage` in `src/adapter/index.ts`).

## Testing setup and pitfalls
- Test environment: custom happy-dom VM + `fake-indexeddb` (`test/environment.ts`).
- API mocking: MSW server with `onUnhandledRequest: 'error'` (`test/vitest.setup.ts`), so every network call in tests must be mocked.
- Use test alias `~` for helpers/mocks (`vitest.config.ts`).
- i18n in tests is strict (`throwOnMissing: true`), so missing locale keys will fail tests.

## Project-specific style constraints
- TypeScript strict + explicit return types are enforced via ESLint (`eslint.config.mjs`).
- Import order is auto-sorted by Prettier plugins (`.prettierrc.json`); keep alias usage consistent (`@` for `src`, `~` for `test`).
- For i18n imports, keep the `vue-i18n` alias behavior from `vite.config.ts` (`vue-i18n/dist/vue-i18n.cjs.js`).

