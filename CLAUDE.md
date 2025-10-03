# spexity Development Guidelines

Auto-generated from all feature plans. Last updated: 2025-09-25

## Active Technologies
- Web frontend: Svelte 5, TypeScript + SvelteKit 2 (current project); Backend (unchanged) Quarkus JVM (not in scope for translation logic). + SvelteKit, Paraglide/Inlang i18n runtime, Playwright (E2E), existing build tooling (Vite). (main)
- Kotlin 2.2.x (JDK 21) + TypeScript (SvelteKit v2) + Quarkus 3.26.x, RESTEasy Reactive, jOOQ 3.19, Flyway, HtmlSanitizer (owasp-sanitizer), Svelte 5, Inlang i18n (003-adding-the-ability)

## Project Structure
```
backend/
web/
e2e-tests/
```

## Commands
npm test [ONLY COMMANDS FOR ACTIVE TECHNOLOGIES][ONLY COMMANDS FOR ACTIVE TECHNOLOGIES] npm run lint

## Code Style
Web frontend: TypeScript + SvelteKit (current project); Backend (unchanged) Quarkus JVM (not in scope for translation logic).: Follow standard conventions

## Recent Changes

<!-- MANUAL ADDITIONS START -->
- Be concise.
- Do NOT overengineer.
- MUST use Svelte v5 syntax and SvelteKit v2 for web.
- MUST use Quarkus v3 and Kotlin v2.2 for backend.
- MUST follow existing patterns.
- MUST NOT fix issues or tests by hacking around them.
- MUST NOT skip any tests.
- For playwright locators use `testid`.
- MUST NOT change any configuration unless explicitly asked.
- MUST clean up after yourself and remove any unused code.
<!-- MANUAL ADDITIONS END -->