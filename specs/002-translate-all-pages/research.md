# Research: Full application localization (English, Arabic RTL, Simplified Chinese)

## Decisions
- Key Naming Convention: `section.component.identifier` (dot-delimited) – rationale: consistent with existing demo and improves grouping in IDE.
- Catalog Organization: Single JSON per locale (retain current structure) – minimizes loader complexity.
- Fallback Policy: Runtime fallback permitted only as safety; release gate enforces zero missing keys via CI script (to be implemented in tasks phase).
- RTL Strategy: Use CSS logical properties (margin-inline, padding-inline, text-align start/end) and `dir="rtl"` on <html> or root wrapper when Arabic active; avoid manual flipping of absolute positions where possible.
- Persistence Mechanism: Paraglide `setLocale` writes locale cookie; reuse existing behavior (cookie path `/`, locale code short form like `ar`, `zh-cn`).
- Formatting Scope: Explicitly excluded – no date/number/currency localization in this feature.

## Inventory Plan
1. Enumerate UI strings by scanning `web/src/routes` and `web/src/lib` components for literals in markup & aria-* attributes.
2. Tag each discovered string with proposed key; group by functional area.
3. Generate interim list for translation update (English unchanged; Arabic & Chinese require entries).
4. Identify potential duplicates (>1 identical English string) → unify under single key where semantics match.

## Risks & Mitigations
| Risk | Impact | Mitigation |
|------|--------|-----------|
| Missed ARIA attribute | Accessibility regression | Add ARIA audit checklist; automated grep for `aria-` in Svelte components |
| RTL visual breakage | Poor UX for Arabic | Use logical properties; create Playwright visual assertions on key pages |
| Bundle size bloat | Performance budget risk | Measure pre/post gzip size; dedupe keys; consider lazy splitting if > +25KB |
| Untranslated new strings sneaking in | Incomplete localization | CI script compares key sets across locales |
| Over-translation of server data | Data inconsistency | Pattern: wrap only static labels; never pass dynamic values to translation keys |

## Alternatives Considered
- Separate namespace files per section: Rejected (adds complexity, no scaling need yet)
- Automatic extraction tooling now: Deferred (manual curation faster for current size)
- Introducing pluralization library: Rejected (out-of-scope per spec decision)

## Open Follow-Ups (Post-Feature)
- Add localized date/number formatting utility (future enhancement)
- Evaluate automated key extraction pipeline if messages exceed 800

## Evidence Collection
(Will capture counts after manual scan – to be appended during implementation tasks.)

## Summary
Research confirms feasibility within performance and accessibility budgets using existing tooling, with focus on complete key coverage, RTL robustness, and strict no-missing-keys CI enforcement.
