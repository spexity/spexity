import { expect, test } from "@playwright/test"
import { App } from "../src/App"

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