# Copilot instructions for frontend module

## Quick links
- [Frontend-specific quick pointers](#frontend-specific-quick-pointers)
- [Build, lint, and test](#build-lint-and-test)
- [Local dev](#local-dev)
- [Coding guidelines](#coding-guidelines)
- [Back to root instructions](#back-to-root-instructions)
- [Root instructions (overview)](../.github/copilot-instructions.md)

Primary reference: Use the repository root file `../.github/copilot-instructions.md` for cross-cutting guidance (env, Docker, CI). This file keeps frontend-only guidance concise.

## Frontend-specific quick pointers
- Dev server: http://localhost:8090 (Vite). Proxies `/api/`, `/auth/`, `/login/oauth2/code/` to backend at http://localhost:8091. Override host with `VITE_HOST`.
- Node: 24.x. Install deps and run dev: `npm install` then `npm run dev`.
- Aliases: `@` → `frontend/src`.
- PWA: `vite-plugin-pwa` enabled; ensure denylist avoids caching API/auth routes.

## Build, lint, and test
- Format/lint/typecheck: `npm run check:format` ; `npm run check:lint`
- Build: `npm run build`
- Tests: `npm run test:coverage` (CET timezone). Uses Vitest + happy-dom + MSW.

## Local dev
- Start backend first (8091) for proxied API and OAuth flows to work.
- On Windows PowerShell, chain commands with `;` when combining steps.

## Coding guidelines
- TypeScript strict.
- ESLint + Prettier (import-sorted). Keep imports ordered.
- i18n: import from `vue-i18n/dist/vue-i18n.cjs.js` when needed.

## Back to root instructions
- See `../.github/copilot-instructions.md` for repository-wide guidance, environment variables, and Docker.
