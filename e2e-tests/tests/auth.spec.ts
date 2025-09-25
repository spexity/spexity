import { expect, test } from "@playwright/test"
import { App } from "../src/App"

// Run auth tests serially to avoid race conditions with authentication state
/*test.describe.configure({ mode: 'serial' })

test.beforeEach(async ({ page, context }) => {
  // Clear all storage to ensure clean state between tests
  await context.clearCookies()
  // Navigate to page first, then clear storage
  await page.goto("/")
  await page.evaluate(() => {
    try {
      localStorage.clear()
      sessionStorage.clear()
    } catch (e) {
      // Ignore storage access errors
    }
  })
})*/

test("sign in", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  await app.accountMenu.open()
  expect(await app.accountMenu.contributorHandle()).toEqual("Ham#1001")
})

test("sign out", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  await app.signOut()
  await app.accountMenu.open()
  await expect(app.accountMenu.getSignInLink()).toBeVisible()
})
