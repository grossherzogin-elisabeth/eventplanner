# AGENTS.md

## Agent quick start (60 seconds)
- Use Java 25 and the Gradle wrapper in `backend/`.
- Default backend run: `./gradlew bootRun --args='--spring.profiles.active=local,secrets'`.
- Default full checks: `./gradlew build test jacocoTestReport`.
- After backend edits, run ArchUnit rules: `./gradlew test --tests org.eventplanner.architecture.ArchUnitRulesTest`.
- For API integration work, use `MockMvc` tests in `src/test/java/org/eventplanner/integration/api/**`.

## Scope and precedence
- This file applies to `backend/` only.
- Use this file + `backend/.github/copilot-instructions.md` first.
- Use root `./.github/copilot-instructions.md` for cross-cutting topics (Docker/CI/frontend interactions).

## Current backend baseline (verified from project files)
- Build system: Gradle (`backend/build.gradle`).
- Spring Boot plugin version: `4.0.6`.
- Java source compatibility: `25`.
- DB: SQLite + Flyway.
- Test framework: JUnit 5 + Spring Boot test starters + ArchUnit.

## Important note about potentially outdated docs
- Root `./.github/copilot-instructions.md` currently states "Spring Boot 3.5" for backend.
- The actual backend build file uses Spring Boot `4.0.6`.
- When uncertain, treat `backend/build.gradle` as source of truth.

## Architecture and layering rules
- Source of truth for architecture constraints:
  - `src/test/java/org/eventplanner/architecture/ArchUnitRulesTest.java`
- High-level intent:
  - Domain isolated from application/rest/adapter.
  - Application isolated from rest/adapter.
  - Adapters (incl. rest) isolated from each other.

## Backend testing playbook

### Unit tests
- Keep tests close to classes under test.
- Prefer deterministic assertions over implementation details.

### Integration tests (API)
- Structure by resource and CRUD action, e.g.:
  - `CreatePositionIntegrationTest`
  - `UpdatePositionIntegrationTest`
  - `DeletePositionIntegrationTest`
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` + security setup in `@BeforeEach`.
- Common auth assertions per endpoint:
  - no auth -> `401`
  - insufficient role -> `403`
- For non-trivial request payloads, store JSON under:
  - `src/test/resources/integration/api/.../*.json`
- Tiny payloads (for example `{}`) may be inline.

### DB-level verification (important)
- For create/update/delete integration tests, assert database state directly via JPA repositories.
- Examples:
  - `PositionJpaRepository`
  - `QualificationJpaRepository`
- Prefer asserting persisted fields (create/update) and `existsById(...)` (delete).

### Test data conventions
- Seed test data lives in Flyway test migrations under:
  - `src/test/resources/db/testdata/`
- Use constants instead of hardcoded UUIDs where available:
  - `src/test/java/org/eventplanner/testdata/PositionKeys.java`
  - `src/test/java/org/eventplanner/testdata/QualificationKeys.java`

## Session lessons captured
- Position and qualification API integration tests were refactored to:
  - dedicated CRUD test classes,
  - JSON request fixtures,
  - DB-level verification instead of list-endpoint-only checks,
  - non-existing resource checks (`404`) for update/delete paths.
- Updating seeded entities in tests is preferred over creating setup entities when feasible.

## Useful run configurations (IntelliJ)
- `Tests in 'org.eventplanner.integration'`
- `Tests in 'org.eventplanner.integration.api.positions'`
- `CreatePositionIntegrationTest`
- `CreateQualificationIntegrationTest`

## Logs and diagnostics
- Backend logs: `backend/logs/`
- Test failure reports: `backend/build/reports/tests/test/index.html`
- XML test details: `backend/build/test-results/test/`

