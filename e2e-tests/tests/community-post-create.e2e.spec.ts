import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test("create community and post works", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()

  const communitiesPage = await app.goToCommunities()
  const communityName = `Community ${crypto.randomUUID().slice(0, 8)}`
  const communityPage = await communitiesPage.createCommunity(communityName)
  await expect(page.getByTestId("create-post-button")).toBeVisible()
  await expect(page.getByText(communityName)).toBeVisible()

  const subject = `Post ${crypto.randomUUID().slice(0, 8)}`
  const body = "This is the body of the post."
  await communityPage.createPost(subject, body)
  await expect(page.getByTestId("post-subject")).toHaveText(subject)
})




