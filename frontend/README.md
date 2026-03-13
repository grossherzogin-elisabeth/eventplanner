# Frontend

Frontend module for the Event Planner app.

## Prerequisites

- Node.js 24.x and npm
- Backend running on `http://localhost:8091` for local API and authentication (see backend README.md)

## Getting started

### Install dependencies

```bash
npm install
```

### Start the dev server

This starts Vite on `http://localhost:8090`.

```bash
npm run dev
```

## Quality checks and build

Please run these before creating a PR:

```bash
npm run format
npm run lint
npm run build
npm run test
```

## Local development details

- Dev server proxies these paths to backend on port `8091`:
  - `/api/`
  - `/auth/`
  - `/login/oauth2/code/`
- You can override the host with `VITE_HOST` for running the app on a local dns name (useful for testing with mobile devices on the same network)
- Tests use Central European Time (`TZ=CET` configured in npm scripts)

## Architecture overview

- `src/domain`: core domain layer with business entities, value objects, and core business logic that does not depend on adapters or transport concerns
- `src/application`: use cases, application services, and adapter ports for business logic that depends on external belongings like REST APIs
- `src/adapter`: infrastructure layer implementing application ports (REST repositories, IndexedDB/in-memory storage)
- `src/ui`: All UI related files with Vue as frontend framework; uses application use cases and domain types
- `src/common`: domain-agnostic shared utilities (for example date helpers and generic utils) that can be reused across layers
- Dependency rule: UI should not call adapter implementations directly; adapter usage is wired through application ports/factories
- Routes are discovered from `src/ui/views/**/Route.ts`
- `@` alias points to `src`, `~` alias points to `test`

## Testing notes

- Test stack: Vitest + happy-dom + MSW + fake IndexedDB
- MSW is strict (`onUnhandledRequest: 'error'`), so every network call must be mocked
- i18n is strict in tests (`throwOnMissing: true`), so missing keys fail tests

