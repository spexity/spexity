#!/usr/bin/env node

/**
 * I18n Key Validator
 *
 * 1. Verifies that all locale files (en.json, ar.json, zh-cn.json) contain the same set of keys
 * 2. Checks for unused translation keys by scanning source code
 *
 * Exits with non-zero code if there are mismatches or unused keys.
 *
 * Usage: node web/scripts/check-i18n-keys.mjs
 */

import { readFileSync, existsSync, readdirSync, statSync } from "fs"
import { join, dirname } from "path"
import { fileURLToPath } from "url"

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const MESSAGES_DIR = join(__dirname, "..", "messages")
const SRC_DIR = join(__dirname, "..", "src")
const LOCALES = ["en", "ar", "zh-cn"]

function loadLocaleFile(locale) {
  const filePath = join(MESSAGES_DIR, `${locale}.json`)

  if (!existsSync(filePath)) {
    console.error(`❌ Missing locale file: ${filePath}`)
    return null
  }

  try {
    const content = readFileSync(filePath, "utf-8")
    return JSON.parse(content)
  } catch (error) {
    console.error(`❌ Invalid JSON in ${filePath}:`, error.message)
    return null
  }
}

function getKeys(obj, prefix = "") {
  const keys = []

  for (const [key, value] of Object.entries(obj)) {
    if (key === "$schema") continue // Skip schema metadata

    const fullKey = prefix ? `${prefix}.${key}` : key

    if (typeof value === "object" && value !== null && !Array.isArray(value)) {
      keys.push(...getKeys(value, fullKey))
    } else {
      keys.push(fullKey)
    }
  }

  return keys
}

function validateKeyParity() {
  console.log("🔍 Checking i18n key parity across locales...\n")

  const localeData = {}
  const localeKeys = {}

  // Load all locale files
  for (const locale of LOCALES) {
    const data = loadLocaleFile(locale)
    if (data === null) {
      return false // Failed to load
    }

    localeData[locale] = data
    localeKeys[locale] = new Set(getKeys(data))

    console.log(`✅ Loaded ${locale}.json (${localeKeys[locale].size} keys)`)
  }

  console.log()

  // Use English as the reference
  const referenceKeys = localeKeys["en"]
  let hasErrors = false

  // Check each non-English locale against English
  for (const locale of LOCALES) {
    if (locale === "en") continue

    const currentKeys = localeKeys[locale]
    const missingKeys = [...referenceKeys].filter((key) => !currentKeys.has(key))
    const extraKeys = [...currentKeys].filter((key) => !referenceKeys.has(key))

    if (missingKeys.length > 0) {
      console.error(`❌ ${locale}.json missing keys:`)
      missingKeys.forEach((key) => console.error(`   - ${key}`))
      hasErrors = true
    }

    if (extraKeys.length > 0) {
      console.error(`❌ ${locale}.json has extra keys:`)
      extraKeys.forEach((key) => console.error(`   - ${key}`))
      hasErrors = true
    }

    if (missingKeys.length === 0 && extraKeys.length === 0) {
      console.log(`✅ ${locale}.json has matching keys with en.json`)
    }
  }

  if (!hasErrors) {
    console.log(`\n✅ All locale files have matching key sets (${referenceKeys.size} keys each)`)
    return true
  } else {
    console.log(`\n❌ Key parity validation failed. Fix missing/extra keys before release.`)
    return false
  }
}

function getAllSourceFiles(dir) {
  const files = []

  function traverse(currentDir) {
    const entries = readdirSync(currentDir)

    for (const entry of entries) {
      const fullPath = join(currentDir, entry)
      const stat = statSync(fullPath)

      if (stat.isDirectory()) {
        // Skip node_modules and other build directories
        if (!['node_modules', '.git', 'dist', 'build', '.svelte-kit'].includes(entry)) {
          traverse(fullPath)
        }
      } else if (stat.isFile() && (entry.endsWith('.svelte') || entry.endsWith('.ts') || entry.endsWith('.js'))) {
        files.push(fullPath)
      }
    }
  }

  traverse(dir)
  return files
}

function findUsedKeys() {
  console.log("\nScanning source files for translation key usage...\n")

  const sourceFiles = getAllSourceFiles(SRC_DIR)
  const usedKeys = new Set()

  // Patterns to match translation function calls
  const patterns = [
    /m\.(\w+)\(/g                    // m.key_name(
  ]

  let totalFiles = 0
  let filesWithKeys = 0

  for (const filePath of sourceFiles) {
    totalFiles++
    const content = readFileSync(filePath, 'utf-8')
    let fileHasKeys = false

    for (const pattern of patterns) {
      let match
      while ((match = pattern.exec(content)) !== null) {
        usedKeys.add(match[1])
        fileHasKeys = true
      }
    }

    if (fileHasKeys) {
      filesWithKeys++
    }
  }

  console.log(`Scanned ${totalFiles} source files (${filesWithKeys} contain translation keys)`)
  console.log(`Found ${usedKeys.size} unique translation keys in use`)

  return usedKeys
}

function validateUnusedKeys() {
  const usedKeys = findUsedKeys()

  // Load English keys as reference
  const enData = loadLocaleFile('en')
  if (!enData) return false

  const allKeys = new Set(getKeys(enData))
  const unusedKeys = [...allKeys].filter(key => !usedKeys.has(key))

  console.log("\nChecking for unused translation keys...\n")

  if (unusedKeys.length > 0) {
    console.error(`❌ Found ${unusedKeys.length} unused translation keys:`)
    unusedKeys.forEach(key => console.error(`   - ${key}`))
    console.error("\nConsider removing unused keys to keep message catalogs clean.")
    return false
  } else {
    console.log(`✅ All ${allKeys.size} translation keys are in use`)
    return true
  }
}

// Run validations
const paritySuccess = validateKeyParity()
const usageSuccess = validateUnusedKeys()

const overallSuccess = paritySuccess && usageSuccess

if (overallSuccess) {
  console.log("\n✅ All i18n validation checks passed!")
} else {
  console.log("\n❌ I18n validation failed. Please fix the issues above.")
}

process.exit(overallSuccess ? 0 : 1)
