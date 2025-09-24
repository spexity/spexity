# Spexity Constitution
<!--
Sync Impact Report
- Version change: 1.0.0 → 1.1.0
- Modified principles: None
- Added sections: AI Agent Conduct
- Removed sections: None
- Templates requiring updates: ✅ No changes required
- Follow-up TODOs: None
-->
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
Constitution Check gates (privacy, ethics, accessibility, performance, simplicity) pass.

Rationale: We codify quality to keep speed without regressions.

## Technology Standards

- Backend: Quarkus on JVM; Database: PostgreSQL.
- Web: Svelte + SvelteKit.
- E2E testing: Playwright. MUST be in Black box testing.
- Security: HTTPS by default; strict CSP; no third-party trackers; logs without PII.
- Observability: Structured, privacy-safe logs and metrics only; no user identifiers.

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

### AI Agent Conduct

- Responses MUST be concise and directly actionable.
- Prefer bullet lists; avoid repeating unchanged plans or context.
- No filler or speculative content; ask a single clarifying question only if blocked.
- Default to minimal formatting; expand only when explicitly requested.

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

**Version**: 1.1.0 | **Ratified**: 2025-09-24 | **Last Amended**: 2025-09-24