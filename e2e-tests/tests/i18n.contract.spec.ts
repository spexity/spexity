import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test.describe("I18n Contract Tests", () => {

  test("basic retrieval - English catalog has key", async ({ page }) => {
    const app = new App(page)
    const homePage = await app.launch()
    await expect(page.getByRole("link", { name: "Home" })).toBeVisible()
    await expect(page.getByRole("link", { name: "Communities" })).toBeVisible()
    await expect(homePage.getLogo()).toHaveAttribute("alt", "Spexity logo")
  })

})

test.describe("RTL Layout Tests", () => {

  test("HTML direction attribute set correctly", async ({ page }) => {
    const app = new App(page)
    await app.launch()
    await expect(page.locator("html")).toHaveAttribute("dir", "ltr")
    await app.changeLanguage("ar")
    await expect(page.locator("html")).toHaveAttribute("dir", "rtl")
    await app.changeLanguage("zh-cn")
    await expect(page.locator("html")).toHaveAttribute("dir", "ltr")
  })

})


test.describe("Language Switch Persistence", () => {

  test("language selection persists after browser reload", async ({ page }) => {
    const app = new App(page)
    await app.launch()
    await expect(page.getByRole("link", { name: "Home" })).toBeVisible()
    await app.changeLanguage("ar")
    await expect(page.getByRole("link", { name: "الرئيسية" })).toBeVisible()
    await expect(page.locator("html")).toHaveAttribute("dir", "rtl")
    const cookies = await page.context().cookies()
    const localeCookie = cookies.find(cookie => cookie.name === "locale")
    expect(localeCookie?.value).toBe("ar")
    await page.reload()
    await app.homePage.awaitPageLoad()
    await expect(page.getByRole("link", { name: "الرئيسية" })).toBeVisible()
    await expect(page.locator("html")).toHaveAttribute("dir", "rtl")
  })

  test("language selection persists across different pages", async ({ page }) => {
    const app = new App(page)
    await app.launch()
    await app.changeLanguage("zh-cn")
    await expect(page.getByRole("link", { name: "首页" })).toBeVisible()
    await page.getByRole("link", { name: "社区", exact: true }).click()
    await page.waitForURL("**/communities")
    await expect(page.getByRole("link", { name: "首页" })).toBeVisible()
    await expect(page.getByRole("link", { name: "社区", exact: true })).toBeVisible()
  })

})
