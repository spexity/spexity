# Tasks: Full application localization of web UI with Arabic and Simplified Chinese

**Input**: Design documents from `/specs/002-translate-all-pages/`
**Prerequisites**: plan.md, research.md, data-model.md, contracts/, quickstart.md

## Execution Flow (main)
(Generated from artifacts – follow numbering strictly; TDD order enforced.)

## Phase 3.1: Setup & Inventory
- [x] T001 Create i18n key inventory: scan `web/src` for user-visible & ARIA strings; output provisional list in `specs/002-translate-all-pages/research.md` (append under **Evidence Collection**).
- [x] T002 [P] Establish key naming map file `web/messages/_key-map.json` (maps discovered literals → final key ids) to aid refactors.
- [x] T003 Add build/CI script `web/scripts/check-i18n-keys.mjs` that verifies key parity across `en.json`, `ar.json`, `zh-cn.json` (exit non-zero if mismatch).
- [x] T004 Integrate parity script into root `test.sh` (invoke after existing tests) without breaking current flows.

## Phase 3.2: Tests First (TDD) – Contract & Integration
- [x] T006 Contract test: translation retrieval success/fallback in `e2e-tests/tests/i18n.contract.spec.ts` (cover cases from i18n-contract.md). Fails initially (missing harness util).
- [x] T007 Contract test: accessibility localization (ARIA label & live region) in `e2e-tests/tests/accessibility.contract.spec.ts` referencing accessibility-contract.md.
- [x] T008 [P] Integration test: language switch persists via cookie in `e2e-tests/tests/language-switch.persist.spec.ts`.
- [x] T009 [P] Integration test: RTL layout for Arabic (direction + key component alignment) in `e2e-tests/tests/rtl-layout.spec.ts`.
- [x] T010 [P] Integration test: No missing keys (simulate removal triggers failure) in `e2e-tests/tests/i18n-parity.spec.ts`.
- [x] T011 [P] Integration test: ARIA live region message updates on language change `e2e-tests/tests/aria-live.spec.ts`.

## Phase 3.3: Core Implementation (Only after T006–T011 fail)
- [x] T012 Implement inventory-driven refactor: replace literals with translation calls from `m.` imported from `$lib/paraglide/messages.js` across `web/src/routes` (batch by folder) – commit after completing each major route group (index, account, shared components). (Single sequential task to avoid merge conflicts.)
- [x] T013 [P] Populate / update `web/messages/en.json` with complete key set (English unchanged for existing keys; add new ones).
- [x] T014 [P] Add Arabic translations to `web/messages/ar.json` (verified translations required).
- [x] T015 [P] Add Simplified Chinese translations to `web/messages/zh-cn.json` (verified translations required).
- [x] T016 Implement language switcher menu item inside Account menu (file path: identify component, likely `web/src/lib/components/AccountMenu.svelte` or add new) adding accessible labels & role semantics.
- [x] T017 Implement cookie persistence integration using existing Paraglide runtime (ensure `setLocale` called; confirm cookie).
- [x] T018 Add RTL direction handling: set `dir="rtl"` when Arabic active; ensure global/root layout (likely `web/src/routes/+layout.svelte` or root HTML). Use logical CSS utilities; adjust icon mirroring if necessary.
- [x] T019 Add accessibility enhancements: ensure all aria-* attributes use localized strings; audit per accessibility-contract.md.

## Phase 3.4: Testing & Validation Pass
- [x] T022 Run parity script (expect pass: zero missing keys) and update `research.md` Evidence Collection with key count per locale.
- [x] T023 Execute Playwright suite (all new specs) – fix any RTL or translation regressions discovered.
- [x] T025 Manual screen reader spot check (VoiceOver / NVDA) for Arabic + Chinese on core flows; document notes in `research.md` Accessibility subsection.
- [x] T026 Confirm no untranslated keys by searching for hard-coded English patterns (grep heuristic) and manually auditing anomalies.

## Phase 3.5: Polish & Hardening
- [x] T029 Documentation refinement: append section to `quickstart.md` describing adding a new locale & RTL considerations.
- [x] T030 Refactor duplicated keys (if any discovered) consolidating to canonical keys; update catalogs & usages.
- [x] T031 Final accessibility audit checklist: ensure all new controls have aria-label or name-from-content; record pass in `research.md`.
- [x] T032 Conventional Commit: `feat(i18n): add full multilingual UI with Arabic RTL and Simplified Chinese support`.

## Dependencies & Parallelization
- T001 precedes T012 (inventory needed before refactor).
- T002 can run parallel with T003 (distinct files). 
- T006–T011 (tests) precede all implementation tasks T012–T021.
- T013–T015 can run in parallel ([P]) once T012 introduces keys (English base may grow—coordinate to avoid merge conflicts; treat English as source-of-truth).
- T016–T019 depend on initial key replacement (T012) and tests.
- T018 depends on language switcher partially (T016) for verifying RTL after switch.
- T022–T026 depend on all implementation tasks.
- Polish tasks (T029–T031) depend on validation pass.

## Parallel Execution Example
```
# After T006–T011 written and failing:
Run in parallel: T013 T014 T015 (locale catalog population)
Then sequential: T016 → T017 → T018 (switcher & RTL) while T019 (ARIA audit) can start once partial key usage exists.
```

## Validation Checklist
- All contract tests (T006–T007) created before implementation.
- All integration tests (T008–T011) exist and fail initially.
- Zero missing keys at T022.
- RTL verified by T023.
- Accessibility & ARIA parity confirmed T025/T031.
- No hard-coded strings prior to final commit (T026/T028).

## Notes
- Keep English text authoritative; do not “improve” existing English phrasing.
- Placeholder mismatch should fail validation scripts early.
