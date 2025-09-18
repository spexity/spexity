import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test("has logo", async ({ page }) => {
  await page.goto('/')
  await expect(page.getByAltText('Spexity logo')).toBeVisible()
})

test("can login", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.login()
  const accountMenu = await app.accountMenu()
  expect(await accountMenu.contributorHandle()).toEqual("Ham#1001")
})