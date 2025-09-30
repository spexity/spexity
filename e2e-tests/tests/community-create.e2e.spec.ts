import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test("create community works", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()

  const communitiesPage = await app.goToCommunities()
  const communityFormPage = await communitiesPage.clickStartCommunity()

  const communityName = `Community ${crypto.randomUUID().slice(0, 8)}`
  await communityFormPage.createCommunity(communityName)

  await expect(page.getByTestId("create-post-button")).toBeVisible()
  await expect(page.getByText(communityName)).toBeVisible()
})




