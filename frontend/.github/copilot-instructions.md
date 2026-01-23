# Copilot instructions for frontend module

## Quick links
- [Frontend-specific quick pointers](#frontend-specific-quick-pointers)
- [When opened outside the repo root](#when-opened-outside-the-repo-root)
- [Where to find details (avoid duplication)](#where-to-find-details-avoid-duplication)

Primary reference: Use the repository root file `../.github/copilot-instructions.md` for full guidance (tech stack, commands, CI/Docker, coding rules). This file only contains minimal, frontend-specific pointers for standalone use.

## Frontend-specific quick pointers
- Dev server runs on http://localhost:8090 (Vite).
- Vite proxies `/api/`, `/auth/`, and `/login/oauth2/code/` to the backend at `http://<host>:8091`. If your backend runs on a different host/port, set `VITE_HOST` accordingly (see `vite.config.ts`).

## When opened outside the repo root
- CI, Docker, and repo-wide practices live at the root; refer to the root instructions.
- For full-stack development, start the backend as described in `../.github/copilot-instructions.md` (Local development workflow → Backend).

## Where to find details (avoid duplication)
- Commands (install/build/lint/test): See root → "Build, lint, and test commands".
- Coding guidelines (TS strictness, ESLint/Prettier, aliases, i18n import, PWA denylist): See root → "Coding guidelines and conventions → Frontend".
- Troubleshooting (Windows PowerShell tips): See root → "Common tips to avoid command failures on Windows (PowerShell)".


