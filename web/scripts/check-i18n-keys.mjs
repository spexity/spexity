#!/usr/bin/env node

/**
 * I18n Key Parity Validator
 *
 * Verifies that all locale files (en.json, ar.json, zh-cn.json) contain
 * the same set of keys. Exits with non-zero code if there are mismatches.
 *
 * Usage: node web/scripts/check-i18n-keys.mjs
 */

import { readFileSync, existsSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const MESSAGES_DIR = join(__dirname, '..', 'messages');
const LOCALES = ['en', 'ar', 'zh-cn'];

function loadLocaleFile(locale) {
  const filePath = join(MESSAGES_DIR, `${locale}.json`);

  if (!existsSync(filePath)) {
    console.error(`❌ Missing locale file: ${filePath}`);
    return null;
  }

  try {
    const content = readFileSync(filePath, 'utf-8');
    return JSON.parse(content);
  } catch (error) {
    console.error(`❌ Invalid JSON in ${filePath}:`, error.message);
    return null;
  }
}

function getKeys(obj, prefix = '') {
  const keys = [];

  for (const [key, value] of Object.entries(obj)) {
    if (key === '$schema') continue; // Skip schema metadata

    const fullKey = prefix ? `${prefix}.${key}` : key;

    if (typeof value === 'object' && value !== null && !Array.isArray(value)) {
      keys.push(...getKeys(value, fullKey));
    } else {
      keys.push(fullKey);
    }
  }

  return keys;
}

function validateKeyParity() {
  console.log('🔍 Checking i18n key parity across locales...\n');

  const localeData = {};
  const localeKeys = {};

  // Load all locale files
  for (const locale of LOCALES) {
    const data = loadLocaleFile(locale);
    if (data === null) {
      return false; // Failed to load
    }

    localeData[locale] = data;
    localeKeys[locale] = new Set(getKeys(data));

    console.log(`✅ Loaded ${locale}.json (${localeKeys[locale].size} keys)`);
  }

  console.log();

  // Use English as the reference
  const referenceKeys = localeKeys['en'];
  let hasErrors = false;

  // Check each non-English locale against English
  for (const locale of LOCALES) {
    if (locale === 'en') continue;

    const currentKeys = localeKeys[locale];
    const missingKeys = [...referenceKeys].filter(key => !currentKeys.has(key));
    const extraKeys = [...currentKeys].filter(key => !referenceKeys.has(key));

    if (missingKeys.length > 0) {
      console.error(`❌ ${locale}.json missing keys:`);
      missingKeys.forEach(key => console.error(`   - ${key}`));
      hasErrors = true;
    }

    if (extraKeys.length > 0) {
      console.error(`❌ ${locale}.json has extra keys:`);
      extraKeys.forEach(key => console.error(`   - ${key}`));
      hasErrors = true;
    }

    if (missingKeys.length === 0 && extraKeys.length === 0) {
      console.log(`✅ ${locale}.json has matching keys with en.json`);
    }
  }

  if (!hasErrors) {
    console.log(`\n🎉 All locale files have matching key sets (${referenceKeys.size} keys each)`);
    return true;
  } else {
    console.log(`\n💥 Key parity validation failed. Fix missing/extra keys before release.`);
    return false;
  }
}

// Run validation
const success = validateKeyParity();
process.exit(success ? 0 : 1);