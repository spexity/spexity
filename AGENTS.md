## Apply to ALL
- MUST be concise.
- MUST NOT overengineer.
- MUST follow existing patterns.
- MUST NOT fix issues or tests by hacking around them.
- MUST NOT skip any tests.
- MUST NOT change any configuration unless explicitly asked.
- MUST clean up after finishing and remove any unused code.

## Backend
location: backend/
- MUST use Quarkus v3 and Kotlin v2.2 for backend.

## Web
location: web/
- MUST use Svelte v5 and SvelteKit v2 syntax.

## E2E-Tests
location: e2e-tests/
- MUST use `testid` for playwright locators.
- MUST NOT change playwright config.