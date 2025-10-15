import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test("create post works", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()

  const communitiesPage = await app.goToCommunities()
  const communityPage = await communitiesPage.openFirstCommunity()
  if (!(await communityPage.isJoined())) {
    await communityPage.join()
  }

  const postFormPage = await communityPage.clickCreatePost()

  const subject = `Hello from E2E ${crypto.randomUUID().slice(0, 8)}`
  const body = "This is the body of the post."
  await postFormPage.createPost(subject, body)

  await expect(page.getByTestId("post-subject")).toHaveText(subject)
})



