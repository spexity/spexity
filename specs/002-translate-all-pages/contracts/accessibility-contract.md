# Contract: Accessibility Localization

## Purpose
Ensure all ARIA-related user-facing strings are localized and parity exists with visible text equivalents.

## Scope
- aria-label
- aria-labelledby (referenced elements contain localized text)
- aria-describedby
- aria-live region announcements (status, alert roles)
- Tooltips and dialog titles

## Rules
1. Every interactive control with visible label must have localized accessible name that matches or complements visible text.
2. Live region messages must switch language immediately after locale change.
3. No mixed-language announcements unless server data embedded inside translated template.
4. Tooltip and dialog keys stored separately (avoid reuse if semantics differ from button label).
5. RTL: Screen reader reading order remains logical; focus order unaffected by direction switch.

## Test Cases
| Case | Setup | Action | Expectation |
|------|-------|--------|-------------|
| Button label + aria-label | Key exists all locales | Switch locale | Name changes to locale text |
| Live region update | Arabic active | Trigger status message | Arabic announced |
| Fallback prevention | All keys present | Build step | Zero missing ARIA keys |
| Mixed content | Server name placeholder | Show message | Placeholder untouched, surrounding text translated |

## Non-Goals
- Automated semantic role validation (handled elsewhere).
- Contrast/color checks.

## Failure Handling
- Missing ARIA key in dev: Warn + English fallback; block release.
