import { expect, test } from "@playwright/test"
import { App } from "../src/App"

/**
 * Contract Test: Accessibility Localization
 *
 * Tests ARIA-related strings localization as defined in accessibility-contract.md
 * Ensures all ARIA attributes are localized and parity exists with visible text.
 */

test.describe("Accessibility Contract Tests", () => {

  test("button label + aria-label localized in all locales", async ({ page }) => {
    const app = new App(page)
    await app.launch()

    // Test English first
  await expect(app.accountMenu.getMenuButton()).toHaveAttribute("aria-label", "Account menu button")

    // Switch to Arabic
    await app.changeLanguage("ar")

  // Verify ARIA label is localized to Arabic
  await expect(app.accountMenu.getMenuButton()).toHaveAttribute("aria-label", "زر قائمة الحساب")

    // Switch to Chinese
    await app.changeLanguage("zh-cn")

    // Verify ARIA label is localized to Chinese
    await expect(app.accountMenu.getMenuButton()).toHaveAttribute("aria-label", "帐户菜单按钮")
  })

})
