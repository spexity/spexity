import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test.describe("I18n Contract Tests", () => {

  test("basic retrieval - English catalog has key", async ({ page }) => {
    const app = new App(page)
    await app.launch()

    // English should be the default locale and show English text
    await expect(page.getByRole("link", { name: "Home" })).toBeVisible()
    await expect(page.getByRole("link", { name: "Communities" })).toBeVisible()
    await expect(app.getLogo()).toHaveAttribute("alt", "Spexity logo")
  })

})

test.describe("RTL Layout Tests", () => {

  test("HTML direction attribute set correctly", async ({ page }) => {
    const app = new App(page)
    await app.launch()

    // Start with LTR (English)
    await expect(page.locator("html")).toHaveAttribute("dir", "ltr")

    // Switch to Arabic
    await app.changeLanguage("ar")

    // Should switch to RTL
    await expect(page.locator("html")).toHaveAttribute("dir", "rtl")

    // Switch to Chinese (should go back to LTR)
    await app.changeLanguage("zh-cn")

    await expect(page.locator("html")).toHaveAttribute("dir", "ltr")
  })

})


test.describe("Language Switch Persistence", () => {

  test("language selection persists after browser reload", async ({ page }) => {
    const app = new App(page)
    await app.launch()

    // Verify we start in English
    await expect(page.getByRole("link", { name: "Home" })).toBeVisible()

    // Switch to Arabic
    await app.changeLanguage("ar")

    // Verify Arabic is active
    await expect(page.getByRole("link", { name: "الرئيسية" })).toBeVisible()
    await expect(page.locator("html")).toHaveAttribute("dir", "rtl")

    // Check that the locale cookie is set
    const cookies = await page.context().cookies()
    const localeCookie = cookies.find(cookie => cookie.name === "locale")
    expect(localeCookie?.value).toBe("ar")

    // Reload the page
    await page.reload()
    await app.awaitAppPage()

    // Verify Arabic persists after reload
    await expect(page.getByRole("link", { name: "الرئيسية" })).toBeVisible()
    await expect(page.locator("html")).toHaveAttribute("dir", "rtl")
  })

  test("language selection persists across different pages", async ({ page }) => {
    const app = new App(page)
    await app.launch()

    // Switch to Simplified Chinese
    await app.changeLanguage("zh-cn")

    // Verify Chinese is active on home page
    await expect(page.getByRole("link", { name: "首页" })).toBeVisible()

    // Navigate to communities page
    await page.getByRole("link", { name: "社区", exact: true }).click()
  await page.waitForURL("**/communities")

    // Verify Chinese persists on communities page
  await expect(page.getByRole("link", { name: "首页" })).toBeVisible()
  await expect(page.getByRole("link", { name: "社区", exact: true })).toBeVisible()

    // Navigate to terms page
    await page.goto("/terms-and-conditions")

    // Verify Chinese persists on terms page
    await expect(page.getByRole("heading", { name: "条款和条件" })).toBeVisible()
  })

})
