# Copilot instructions for backend module

## Quick links
- [Backend-specific quick pointers](#backend-specific-quick-pointers)
- [Architecture rules](#architecture-rules)
- [Assistant automation](#assistant-automation)
- [When opened outside the repo root](#when-opened-outside-the-repo-root)
- [Where to find details (avoid duplication)](#where-to-find-details-avoid-duplication)
- [Back to root instructions](#back-to-root-instructions)
- [Root instructions (overview)](../.github/copilot-instructions.md)

Primary reference: Use the repository root file `../.github/copilot-instructions.md` for cross-cutting guidance (tech stack, commands, CI/Docker). This file keeps backend-only guidance concise.

## Backend-specific quick pointers
- Local dev port: http://localhost:8091 (Spring Boot). Some routes require OIDC.
- DB: SQLite at `../data/eventplanner.db`; Flyway runs on startup. Logs in `./logs`.
- Gradle wrapper: `./gradlew build test jacocoTestReport` (Java 25 required).

## Architecture rules
- Source of truth: `src/test/java/org/eventplanner/architecture/ArchUnitRulesTest.java` (must pass).
- Layering: Domain has no deps on application/rest/adapter. Application has no deps on rest/adapter. Adapters (incl. rest) isolated; no adapter-to-adapter deps.
- Adapters/rest must not depend on internal application layers: `application.services`, `application.usecases`, `application.scheduled`, `application.ports`.
- Application/domain must not depend on: `software.amazon.awssdk..`, `jakarta.persistence..`, `org.springframework.data.jpa..`, `io.swagger..`.
- Controllers must return `ResponseEntity` or `RedirectView`.
- Nullability: Outside `org.eventplanner..rest..`, annotate non-primitive return types and all parameters/ctor params with `@NonNull` or `@Nullable` (exceptions: `@Value`, `@Autowired`, primitives, generated Lombok/record/enum functions).

## Assistant automation
- After each backend edit, run: `./gradlew test --tests org.eventplanner.architecture.ArchUnitRulesTest` from `backend/` and fix violations.
- Assistant behavior is documented in `backend/.github/skills/archUnitTest/SKILL.md`: it will iterate up to three focused fixes, re-running the test each time, and summarize failures with fix options if blocked.
- CI enforces this in `.github/workflows/validate_code.yml` with a dedicated step “Run ArchUnit rules”.

## When opened outside the repo root
- If relative paths (data/logs) fail, set IntelliJ run configuration working directory to the repo root or adjust paths.
- CI, Docker, and env details live at the root instructions.

## Where to find details (avoid duplication)
- Commands (build/test/run, JaCoCo): see "Build, lint, and test" in `../.github/copilot-instructions.md`.
- Config and secrets (profiles/env vars): see "Local development" and "Configuration (env)" in `../.github/copilot-instructions.md`.
- General coding guidelines: see "Coding guidelines (essentials)" in `../.github/copilot-instructions.md`.

## Back to root instructions
- See `../.github/copilot-instructions.md` for repository-wide guidance and quickstarts.
