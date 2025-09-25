# Data Model (Conceptual) – Localization Feature

## Entities

### MessageKey
Represents a logical user-visible string identifier.
- Attributes: `id` (string, dot-delimited), `description` (editorial guidance), `placeholders` (list of variable names if any)
- Rules: Must exist in all locale catalogs; English is canonical text.

### LocaleCatalog
Collection of translations for one locale.
- Attributes: `localeCode` (e.g., `en`, `ar`, `zh-cn`), `entries` (map<MessageKey.id,string>)
- Rules: Key set must equal English key set at build time.

### LanguagePreference
Stores currently selected locale for a user agent.
- Attributes: `cookieName` (string), `localeCode` (string)
- Behavior: Read during initial load; updated via language switcher; persisted as cookie.

### RtlLayoutRules
Defines conditions under which RTL is applied.
- Attributes: `isRtl(localeCode)` (boolean)
- Behavior: Returns true for `ar`; triggers `dir="rtl"` and logical CSS adjustments.

### AccessibilityString
Represents ARIA-specific key usage distinct from visible label.
- Attributes: `messageKeyId`, `targetRole`, `attributeType` (label, describedby, live, tooltip), `dynamic` (boolean)
- Rules: Must have translation parity with visible label if they convey the same concept.

## Relationships
- LocaleCatalog 1..1 ↔ n MessageKey (catalog stores value for each key).
- LanguagePreference 1..1 current LocaleCatalog (by localeCode).
- RtlLayoutRules applies to LanguagePreference.localeCode.
- AccessibilityString references MessageKey.

## Invariants
- All LocaleCatalogs share identical key sets.
- No hard-coded UI string appears outside translation retrieval calls.
- Arabic locale triggers RTL layout at initialization (before first paint where feasible).
- Missing key count == 0 at release build gate.

## State & Transitions
1. Initial load: LanguagePreference resolved → locale catalogs loaded → direction set.
2. User switches language: LanguagePreference updated → UI re-renders → direction updated if needed.
3. Build step: Validation ensures key parity across locales.

## Validation Rules
- Reject build if any locale missing key present in English.
- Warn (non-blocking) if description metadata absent for new keys (encourage clarity).
- Disallow placeholder count mismatch across locales.

## Notes
This feature does not introduce persistence beyond cookie; no backend schema changes required.
