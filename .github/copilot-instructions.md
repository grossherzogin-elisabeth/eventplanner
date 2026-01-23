# Eventplanner

## How to use these instructions
- For backend changes, read `backend/.github/copilot-instructions.md` (module-specific). For frontend, read `frontend/.github/copilot-instructions.md`.
- Use this root file for a fast overview and cross-cutting practices (CI, Docker, env, architecture rules).

## Quick links
- [What this app does](#what-this-app-does)
- [Tech stack](#tech-stack)
- [Repository map](#repository-map)
- [Configuration (env)](#configuration-env)
- [Local development](#local-development)
- [Build, lint, and test](#build-lint-and-test)
- [Coding guidelines (essentials)](#coding-guidelines-essentials)
- [Architecture rules (backend)](#architecture-rules-backend)
- [Tools and CI](#tools-and-ci)
- [Quickstarts](#quickstarts)
- [Known notes](#known-notes)

## What this app does
- Plan multi-day events with scheduling, crew management, notifications, and exports.
- Single Spring Boot service serving REST APIs and the built Vue frontend. Local dev runs Vue on Vite and proxies to the backend.

## Tech stack
- Backend: Java 25, Spring Boot 3.5, Hibernate, Flyway, SQLite, Freemarker, Lombok, Micrometer/Prometheus, Apache POI. Build via Gradle wrapper.
- Frontend: Vue 3 + TypeScript, Vite, Tailwind CSS, PWA (vite-plugin-pwa), ESLint + Prettier, Vitest + happy-dom + MSW.
- Docker: Multi-stage (Node 24, Temurin JDK 25), one container serving both.
- CI: GitHub Actions (lint, typecheck, build, tests, coverage, Docker, SonarCloud).

## Repository map
- Root: Dockerfile, Dockerfile-local, build_docker.sh, README.md, .tool-versions, `.github/` (workflows, CODEOWNERS, dependabot, this file).
- `backend/`: Spring Boot app (gradle wrapper, build.gradle, settings.gradle, `src/main`, `src/test`).
- `frontend/`: Vite app (`package.json`, `vite.config.ts`, `src/`, `tests/`).
- `data/`: SQLite DB files for local dev. `logs/`: Rolling Spring logs. `src/`: extra non-Java resources packaged for Docker.

## Configuration (env)
- OIDC: `OAUTH_ISSUER_URI`, `OAUTH_CLIENT_ID`, `OAUTH_CLIENT_SECRET` (required for login).
- URL/ports: `PORT`, `PROTOCOL`, `HOST`, `HOST_URL` (for OAuth redirects when ports differ).
- Security/PII: `DATA_ENCRYPTION_PASSWORD` (+ rotation), email behavior: `EMAIL_WHITELIST`, `ADMIN_EMAILS`.
- Defaults and comments: `backend/src/main/resources/application.yml`.

## Local development
- Prereqs: Java 25 (Temurin) and Node 24 (npm). Windows PowerShell: use `;` to chain commands.
- Backend (local + secrets profiles; DB at `data/eventplanner.db`):
  - In `backend/`: `./gradlew bootRun --args='--spring.profiles.active=local,secrets'`
  - Provide OIDC via env or `application-secrets.yml`.
- Frontend (Vite on 8090, proxy to backend 8091):
  - In `frontend/`: `npm install`; `npm run dev`
  - Proxy for `/api/`, `/auth/`, `/login/oauth2/code/` targets `http://<host>:8091` (override host with `VITE_HOST`).
- Docker: `docker build .` then run with the env vars above; container serves on port 80 by default.

## Build, lint, and test
- Frontend: `npm run check:format`; `npm run check:lint`; `npm run build`; `npm run test:coverage` (CET timezone).
- Backend: `./gradlew build test jacocoTestReport` (Java 25 required).

## Coding guidelines (essentials)
- Frontend: TS strict; ESLint + Prettier (import-sorted); alias `@` → `frontend/src`; i18n import `vue-i18n/dist/vue-i18n.cjs.js`; keep PWA denylist to avoid caching API/auth routes.
- Backend: Controllers under `org.eventplanner..` return `ResponseEntity` or `RedirectView`; validate inputs; use `@Transactional` as needed; DB via Flyway migrations in `resources/db/migration` (no runtime schema changes); OAuth2 client configured in `application.yml`. CSRF currently disabled—be cautious with POST/forms.
- General: Keep CI Node/Java versions stable with workflows; update Sonar settings in `backend/build.gradle` if paths move.

## Architecture rules (backend)
- Enforced by ArchUnit (`backend/src/test/java/org/eventplanner/architecture/ArchUnitRulesTest.java`).
- Layering: Domain → no deps on application/rest/adapter. Application → no deps on rest/adapter. Adapters (incl. rest) isolated; no adapter-to-adapter deps.
- Adapters/rest must not depend on internal application layers: `application.services`, `application.usecases`, `application.scheduled`, `application.ports`.
- Application/domain must not depend on: `software.amazon.awssdk..`, `jakarta.persistence..`, `org.springframework.data.jpa..`, `io.swagger..`.
- Controllers must return `ResponseEntity` or `RedirectView`.
- Nullability: Outside `org.eventplanner..rest..`, annotate non-primitive return types and all parameters/ctor params with `@NonNull` or `@Nullable` (exceptions: `@Value`, `@Autowired`, primitives, generated Lombok/record/enum functions).

## Tools and CI
- Workflows: `.github/workflows/*` (validate code, docker build, release).
- Dependabot and CODEOWNERS are configured. Logs: `backend/logs/spring.log`.
- SonarCloud project: `grossherzogin-elisabeth_eventplanner`.

## Quickstarts
- Frontend only:
  - `cd frontend`
  - `npm install`
  - `npm run dev`
- Full local stack:
  - `cd backend` ; `./gradlew bootRun --args='--spring.profiles.active=local,secrets'`
  - `cd ../frontend` ; `npm install` ; `npm run dev`

## Known notes
- `application.yml`: CSRF temporarily disabled. Consider security implications for new POST endpoints/forms.
