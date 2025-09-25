# Contract: Translation Retrieval

## Purpose
Guarantee consistent retrieval of localized user-visible and accessibility strings with zero missing keys at release.

## Interface (Conceptual)
```
getMessage(key: string, locale: string, placeholders?: Record<string,string|number>): string
```
- Inputs:
  - `key`: Dot-delimited MessageKey id.
  - `locale`: Target locale code (`en`, `ar`, `zh-cn`).
  - `placeholders`: Optional map for interpolation.
- Output: Localized string with placeholders substituted.
- Errors:
  - Missing key: SHOULD NOT occur in production build; during development returns English + logs warning.
  - Placeholder mismatch: Throws (development) or returns safe English fallback.

## Constraints
- Must not mutate server-provided data values.
- Must be side-effect free (aside from development warnings).

## Fallback Policy
- Development: Log warning and use English if key absent in non-English locale.
- Production: Build pipeline prevents deployment with missing keys.

## Test Cases
| Case | Given | When | Then |
|------|-------|------|------|
| Basic retrieval | `en` catalog has key | getMessage | Returns English text |
| Arabic retrieval | `ar` catalog has key | getMessage locale=ar | Returns Arabic text (RTL unaffected) |
| Missing Arabic key (dev) | English only | getMessage locale=ar | Returns English + warning |
| Placeholder substitution | Key with `{name}` | getMessage placeholders | Value injected |
| Placeholder mismatch | Arabic missing `{name}` | getMessage | Error or safe fallback (dev) |

## Non-Goals
- Formatting numbers/dates (explicitly out of scope).
- Pluralization logic.
