# Copilot instructions for backend module

## Quick links
- [Backend-specific quick pointers](#backend-specific-quick-pointers)
- [When opened outside the repo root](#when-opened-outside-the-repo-root)
- [Where to find details (avoid duplication)](#where-to-find-details-avoid-duplication)

Primary reference: Use the repository root file `../.github/copilot-instructions.md` for full guidance (tech stack, commands, CI/Docker, coding rules). This file only contains minimal, backend-specific pointers for standalone use.

## Backend-specific quick pointers
- Local dev port: http://localhost:8091 (Spring Boot). Some routes require OIDC.
- DB: SQLite file DB at `../data/eventplanner.db`; Flyway runs on startup. Logs under `../backend/logs`.
- Architecture: Follow rules summarized in the root instructions (see "Architecture rules (backend)" in `../.github/copilot-instructions.md`), derived from `src/test/java/org/eventplanner/architecture/ArchUnitRulesTest.java`.

## When opened outside the repo root
- CI, Docker, and repo-wide practices live at the root; refer to the root instructions.
- If relative paths (data/logs) fail, set the IntelliJ run configuration working directory to the repo root or adjust paths.

## Where to find details (avoid duplication)
- Commands (build/test/run, JaCoCo): see "Build, lint, and test" in `../.github/copilot-instructions.md`.
- Config and secrets (profiles/env vars): see "Local development" and "Configuration (env)" in `../.github/copilot-instructions.md`.
- Coding guidelines (controllers return ResponseEntity/RedirectView, validation, `@Transactional`): see "Coding guidelines (essentials)" and "Architecture rules (backend)" in `../.github/copilot-instructions.md`.
