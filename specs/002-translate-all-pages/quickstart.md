# Quickstart: Working with Localization (This Feature)

## 1. Adding a New UI String
1. Pick key: `area.component.intent` (dot-delimited).
2. Add English text to `web/messages/en.json` (canonical source; do NOT edit existing English strings for prior keys).
3. Add best-effort Arabic (`ar.json`) and Simplified Chinese (`zh-cn.json`) values.
4. If ARIA-specific, create distinct key (avoid reusing visible label key if wording diverges).
5. Run build or i18n validation script (TBD in tasks) to confirm zero missing keys.

## 2. Using in Svelte Component
```
<script lang="ts">
  import { getMessage } from '$lib/i18n'; // conceptual
</script>
<button aria-label={getMessage('nav.account.open')}>
  {getMessage('nav.account')}
</button>
```

## 3. Language Switching
1. Open Account menu.
2. Choose Language item → select new locale.
3. Cookie updated; page strings + ARIA update instantly; Arabic sets `dir="rtl"`.

## 4. RTL Verification
- Inspect root element for `dir="rtl"` when Arabic selected.
- Ensure flex/grid layouts rely on logical properties; adjust if icons/chevrons need mirroring.

## 5. Accessibility Checklist (Per PR)
- All new interactive elements have localized accessible names.
- All aria-live announcements localized.
- No English leakage in non-English locales (except server data).

## 6. Testing
Run Playwright suite (post tasks creation) including:
- Language switch persists after reload
- RTL layout assertion (direction + alignment)
- ARIA announcement localization
- No missing keys test (script exit non-zero if mismatch)

## 7. Adding a Locale (Future)
(Not in current scope) – add new locale file, extend supported locales list, supply full key set before enabling.

## 8. Anti-Patterns
- Direct hard-coded strings inside markup.
- Reusing a visible label key for an ARIA description with different nuance.
- Injecting translated content into server-provided free-form text.

## 9. Release Gate
Release blocked if any locale missing any key present in English.
