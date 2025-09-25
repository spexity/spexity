# Implementation Plan: Full application localization of web UI with Arabic and Simplified Chinese

**Branch**: `002-translate-all-pages` | **Date**: 2025-09-25 | **Spec**: ./spec.md
**Input**: Feature specification from `/specs/002-translate-all-pages/spec.md`

## Execution Flow (/plan command scope)
```
1. Load feature spec from Input path
2. Fill Technical Context (scan for NEEDS CLARIFICATION)
3. Fill the Constitution Check section based on the constitution document
4. Evaluate Constitution Check section (Initial)
5. Execute Phase 0 → research.md
6. Execute Phase 1 → contracts, data-model.md, quickstart.md, agent-specific file
7. Re-evaluate Constitution Check (Post-Design)
8. Plan Phase 2 approach (do NOT create tasks)
9. STOP (ready for /tasks)
```

## Summary
Provide complete multilingual UI for all existing web pages using existing Paraglide/Inlang setup. Support English (source of truth), Arabic (full RTL mirrored UI), and Simplified Chinese. All user-visible UI strings (visual + ARIA) externalized to message catalogs; server-provided data remains unaltered. Language switching is available via an Account menu language item; preference persists via cookie. No locale-specific numeric/date/currency formatting or pluralization in this feature. Release acceptance requires zero missing translation keys (fallback only a safety net, not a delivery condition).

## Technical Context
**Language/Version**: Web frontend: TypeScript + SvelteKit (current project); Backend (unchanged) Quarkus JVM (not in scope for translation logic).  
**Primary Dependencies**: SvelteKit, Paraglide/Inlang i18n runtime, Playwright (E2E), existing build tooling (Vite).  
**Storage**: N/A for feature (language pref cookie only).  
**Testing**: Playwright for end-to-end (add i18n coverage + RTL assertions); existing unit tests (if any) not central.  
**Target Platform**: Modern browsers (desktop + mobile) with screen reader support.  
**Project Type**: web (uses both `web/` and possibly backend unchanged).  
**Constraints**: No new third-party i18n frameworks; must reuse existing Paraglide message infrastructure; RTL must not break keyboard navigation; zero untranslated keys at release.  
**Scale/Scope**: All current pages/routes under `web/src/routes/` and shared components. Estimated < 400 distinct messages after consolidation (assumption; to be validated in Phase 0).  

## Constitution Check
Mapping design to constitution principles (Initial):
- Privacy-First: No PII added; cookie stores only locale code.
- Ethical UX: Language switcher is user-initiated; no dark patterns.
- Accessibility: ARIA strings translated; RTL keyboard and focus order validated.
- Simplicity: Reuse existing paraglide i18n; no new deps.
- Testing: Add Playwright scenarios (language switch, RTL layout, ARIA labels, fallback absence check).
- Internationalization: All strings externalized; RTL Arabic confirmed; zero missing keys target.

Initial Constitution Check: PASS (no violations requiring Complexity Tracking).

## Project Structure
Using existing repository layout (web + backend). Only `web/` subtree modified plus specs artifacts.

**Structure Decision**: Option 2 (Web application)

## Phase 0: Outline & Research
(Completed – see `research.md`)
- Key naming, catalog organization, fallback policy, RTL strategy, inventory workflow, risk mitigations decided.

## Phase 1: Design & Contracts
(Completed – artifacts generated.)
- `data-model.md`, `contracts/i18n-contract.md`, `contracts/accessibility-contract.md`, `quickstart.md`, agent context updated (`CLAUDE.md`).

## Phase 2: Task Planning Approach (Preview Only)
- Derive tasks from: research inventory (string extraction), data-model conceptual docs, pseudo-contract enforcement (Playwright tests), RTL audits.
- Ordering: (1) Inventory & key extraction, (2) Catalog completion, (3) Introduce language switcher UI changes, (4) RTL CSS adjustments, (5) ARIA audit & fixes, (6) Add Playwright tests (multilingual + RTL), (7) Add CI guard for untranslated keys, (8) Final verification.
- Parallelizable ([P]): Inventory vs initial switcher stub; test scaffolding vs CSS adjustments.

## Complexity Tracking
(No entries – no constitutional violations.)

## Progress Tracking
**Phase Status**:
- [x] Phase 0: Research complete (/plan command)
- [x] Phase 1: Design complete (/plan command)
- [ ] Phase 2: Task planning complete (/plan command - describe approach only)
- [ ] Phase 3: Tasks generated (/tasks command)
- [ ] Phase 4: Implementation complete
- [ ] Phase 5: Validation passed

**Gate Status**:
- [x] Initial Constitution Check: PASS
- [x] Post-Design Constitution Check: PASS
- [x] All NEEDS CLARIFICATION resolved
- [ ] Complexity deviations documented

---
*Based on Spexity Constitution v1.3.0 - See `/.specify/memory/constitution.md`*
