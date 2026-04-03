# AGENTS.md

## Agent quick start (60 seconds)
- Use Node 24.x assumptions; start with `npm install` then `npm run dev` (Vite on `http://localhost:8090`, proxies to backend `:8091`).
- Before handing off changes, run: `npm run format`, `npm run lint`, `npm run build`, `npm run test:coverage`.
- Domain is the core business layer (`src/domain`); UI consumes application use cases and selected domain types; adapter integration stays behind application ports.
- Keep auth/fetch/cookie details in REST adapters (for example `src/adapter/rest/*.ts`, CSRF via `src/adapter/util/Csrf.ts`).
- Add new dependencies through `useX()` factories and `reset...()` hooks in layer `index.ts` files (`src/adapter`, `src/application/services`, `src/application/usecases`, `src/domain/services`).
- Routes are discovered from `src/ui/views/**/Route.ts`; each route module exports one default `RouteRecordRaw`.
- Guard behavior comes from route `meta` (`authenticated`, `permissions`) in `src/ui/plugins/router.ts`; failed auth stores `login-redirect` in `localStorage`.
- Tests use Vitest + happy-dom + fake IndexedDB; MSW is strict (`onUnhandledRequest: 'error'`) so every network call must be mocked.
- i18n is strict in tests (`throwOnMissing: true`), so missing locale keys fail tests immediately.

## Scope and precedence
- This file applies to `frontend/` only.
- Prefer `frontend/.github/copilot-instructions.md` and active config files over `README.md` when they disagree.

## Fast start (verified from repo config)
- Install and run: `npm install`, `npm run dev`.
- Recommended pre-PR checks: `npm run format`, `npm run lint`, `npm run build`, `npm run test:coverage`.
- Tests run with `TZ=CET` and Vitest (`package.json`).

## Architecture map (follow this layering)
- Domain (`src/domain`) contains core entities, value objects, and business logic independent of transport/infrastructure.
- Application (`src/application`) orchestrates workflows via use cases/services and defines ports for external dependencies.
- Adapters (`src/adapter`) implement application ports (REST + storage) and own transport/storage details.
- UI (`src/ui`) handles rendering/routing and uses application use cases plus selected domain types.
- Common (`src/common`) contains domain-agnostic shared utilities reusable across layers.
- UI should not call adapter implementations directly; adapter usage is wired through application ports/factories.
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
- Prefer shared fixtures from `~/mocks` (see `test/mocks/index.ts`) instead of ad-hoc inline entity factories in specs.
- Use test names in the `it('should ...')` style for consistency across specs.
- Name the tested unit `testee`; for specs with repeated dependency wiring, define reusable DI requirements in `beforeEach()` (see `test/application/services/EventCachingService.spec.ts`).
- i18n in tests is strict (`throwOnMissing: true`), so missing locale keys will fail tests.

## Project-specific style constraints
- TypeScript strict + explicit return types are enforced via ESLint (`eslint.config.mjs`).
- Import order is auto-sorted by Prettier plugins (`.prettierrc.json`); keep alias usage consistent (`@` for `src`, `~` for `test`).
- For i18n imports, keep the `vue-i18n` alias behavior from `vite.config.ts` (`vue-i18n/dist/vue-i18n.cjs.js`).

