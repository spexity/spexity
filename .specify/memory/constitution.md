# Spexity Constitution
<!--
Sync Impact Report
- Version change: 1.2.0 → 1.3.0
- Modified principles: None
- Added sections:
	- VI. Internationalization and Localization (i18n/L10n)
	- Quality gates updated to include i18n compliance
	- Technology Standards updated with i18n and supported locales
- Removed sections: None
- Templates requiring updates:
	✅ Updated: .specify/templates/plan-template.md
	✅ Updated: .specify/templates/spec-template.md
	✅ Updated: .specify/templates/tasks-template.md
- Follow-up TODOs: None
-->

## Core Principles

### I. Privacy-First (No Identifying Telemetry)
Spexity MUST NOT collect, store, or transmit user-identifying telemetry. No third-party
trackers, ad beacons, or fingerprinting libraries are allowed. Aggregate operational
metrics MAY be collected only if they are strictly non-identifying and cannot be combined
to re-identify individuals. Logs MUST exclude PII by default and be scrubbed at sources.

Rationale: Communities deserve a privacy-respecting platform with trust by design.

### II. Ethical, Non-Addictive UX
The product MUST avoid dark patterns and addictive mechanics. The platform MUST NOT display
ads, MUST NOT autoplay video, and MUST NOT present non–user-initiated pop-ups or dialogs.
Automated agents MUST NOT masquerade as users; any system automation MUST be explicitly
labeled and constrained.

Rationale: The mission is healthy, democratic communication, not engagement hacking.

### III. Accessibility and Inclusive Performance
The system MUST be accessible to people with disabilities, users on low-end devices, and
users with spotty connectivity. Minimum bar:
- Conform to WCAG 2.1 AA for core user flows (semantics, labels, focus order, contrast).
- Full keyboard navigation for interactive features; screen-reader friendly landmarks.
- Works reliably on constrained networks (graceful degradation, progressive enhancement).

Performance budgets (initial targets):
- Web initial route critical resources ≤ 250KB gzip, ≤ 3 network round-trips before
	interactivity on a reference low-end device.
- Backend p95 latency for core endpoints ≤ 200ms under expected test load.

Rationale: Accessibility and performance unlock participation for everyone.

### IV. Simplicity and Proven Technology
Prefer minimal, proven solutions. Prohibited without explicit, documented exception:
- Frameworks/libraries younger than 5 years or without active maintenance.
- Homebrew DSLs.
- Frivolous dependencies (every addition MUST have a clear, documented rationale).

Rationale: Stability, maintainability, and efficiency come from disciplined choices.

### V. Testing and Quality Discipline
Every primary user flow MUST have Playwright end-to-end coverage. Every public backend
endpoint MUST have contract tests. Tests SHOULD be written before implementation when
feasible (red → green → refactor). CI MUST block merges unless all tests pass and
Constitution Check gates (privacy, ethics, accessibility, performance, simplicity, i18n) pass.

Rationale: We codify quality to keep speed without regressions.

### VI. Internationalization and Localization (i18n/L10n)
All new web-facing features MUST include complete translations for all supported locales
and MUST avoid hard-coded user-visible strings in source code.

Non-negotiable rules:
- Supported locales are configured inside `web/project.inlang/settings.json`.
- All user-visible copy MUST be externalized to message keys managed by the i18n system
	(Inlang) and referenced from UI via the i18n utilities.
- New features MUST provide translations for every supported locale in commit scope.
- RTL considerations for Arabic MUST be respected (layout, alignment, icons, and
	mirroring where appropriate). Components MUST support `dir` changes.

Rationale: Internationalization ensures inclusivity and a first-class experience across
languages and directions from day one of each feature.

## Technology Standards

- Backend: Quarkus on JVM; Database: PostgreSQL.
- Web: Svelte + SvelteKit.
- E2E testing: Playwright. MUST be in Black box testing.
- Security: HTTPS by default; strict CSP; no third-party trackers; logs without PII.
- Observability: Structured, privacy-safe logs and metrics only; no user identifiers.
- Internationalization: Inlang project under `web/project.inlang/` with message catalogs
	in `web/messages/`. Supported locales in `web/project.inlang/settings.json`.

## Development Workflow & Quality Gates

- Branch naming: `feature/[###-feature-name]` for feature work tracked under `specs/`.
- Documents: Each feature follows `spec → plan → tasks` flow using templates under
	`.specify/templates/`.
- Code review: At least one reviewer required; reviewers MUST run the Constitution Check.
- Quality gates (CI):
	1) All tests green (contract + e2e + unit where applicable)
	2) Accessibility checks pass for core flows (WCAG 2.1 AA)
	3) Performance budgets satisfied or explicitly waived with rationale and owner
	4) Privacy/ethics gates pass (no telemetry, no dark patterns, no autoplay/ads)
	5) Dependency additions justified and approved
	6) Internationalization: No hard-coded user-visible strings; translations present for
	   locales in `web/project.inlang/settings.json`; RTL behavior verified for affected components

### AI Agent Conduct

- Responses MUST be concise and directly actionable.
- Prefer bullet lists; avoid repeating unchanged plans or context.
- No filler or speculative content; ask a single clarifying question only if blocked.
- Default to minimal formatting; expand only when explicitly requested.

### Commit Practices

- After finishing implementation, the agent MUST create a git commit using Conventional
	Commits (e.g., feat:, fix:, docs:, refactor:, chore:, perf:).
- Commit messages MUST describe the overall intent and outcome, not restate literal code
	diffs (avoid "update code"; prefer "feat: add moderation queue for posts").

## Governance

- Supremacy: This Constitution supersedes style guides and team preferences when in
	conflict.
- Amendments: Changes require a PR with a summary of impacts, updated version number,
	and migration/mitigation notes when needed. Approval by a maintainer with Constitution
	stewardship responsibility is REQUIRED.
- Versioning Policy: Semantic versioning for governance.
	- MAJOR: Incompatible rule changes or removals.
	- MINOR: New principles/sections or materially expanded guidance.
	- PATCH: Clarifications or non-semantic edits.
- Compliance Reviews: Each PR includes a Constitution Check section in the plan and/or
	spec. CI MUST verify gates where automated. Manual verification MUST be recorded in the
	PR description for the remaining gates.

**Version**: 1.3.0 | **Ratified**: 2025-09-24 | **Last Amended**: 2025-09-24