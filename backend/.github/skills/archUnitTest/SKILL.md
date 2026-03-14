# Skill: Enforce backend architecture rules (ArchUnit) after edits

Purpose
- Ensure every backend change respects the project’s architecture rules by automatically running the targeted ArchUnit test and fixing violations before concluding.

Scope
- Applies to edits within `backend/`.
- Uses the dedicated test class: `org.eventplanner.architecture.ArchUnitRulesTest`.

Contract
- Trigger: After any backend file edit or creation.
- Action: Run `./gradlew test --tests org.eventplanner.architecture.ArchUnitRulesTest` from `backend/`.
- Success: Tests pass (green). If violations occur, propose and implement focused fixes.
- Iteration policy: Try up to 3 targeted fixes; re-run the test after each fix.
- Blocked case: If still failing after 3 iterations or a violation requires larger refactoring, summarize:
  - The exact failing rule(s) and message(s)
  - Root cause hypothesis
  - Concrete fix options (small, medium, larger), with estimated impact

Process
1) Detect backend edits.
2) Run the ArchUnit test:
   - Command (Unix/Git Bash): `cd backend; ./gradlew test --tests org.eventplanner.architecture.ArchUnitRulesTest`
   - Command (Windows PowerShell): `cd backend; ./gradlew.bat test --tests org.eventplanner.architecture.ArchUnitRulesTest`
3) If failing, inspect failure output and apply minimal, rule-compliant changes:
   - Maintain layering boundaries: domain → application → adapters (rest); avoid forbidden dependencies.
   - Controllers must return `ResponseEntity` or `RedirectView`.
   - Respect nullability annotations outside `org.eventplanner..rest..`.
4) Re-run the test. Repeat up to 3 times.
5) If still failing, provide a concise summary and recommended resolution steps.

Edge cases
- New endpoints or adapters: ensure no cross-adapter dependencies and application/domain layers don’t depend on infra libs.
- Package moves/renames: update imports to maintain allowed dependencies.
- Generated or test-only code: ensure tests align with rules but don’t weaken rule coverage.

Notes
- CI already enforces this test; this skill ensures local, assistant-driven compliance.
- No git hooks are required; this is an assistant behavior.

References
- Rules source of truth: `backend/src/test/java/org/eventplanner/architecture/ArchUnitRulesTest.java`
- Backend instructions: `backend/.github/copilot-instructions.md`
- Root overview: `.github/copilot-instructions.md`
