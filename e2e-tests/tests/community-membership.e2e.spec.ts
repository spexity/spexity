import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test("join community shows posts in timeline", async ({ page }) => {
  const app = new App(page)
  const homePage = await app.launch()
  await app.signIn()

  const communityName = `Community ${crypto.randomUUID().slice(0, 8)}`
  const communityPage = await (await app.goToCommunities()).createCommunity(communityName)

  const subject = `Post ${crypto.randomUUID().slice(0, 8)}`
  const body = "This post should appear in timeline if joined."
  await communityPage.createPost(subject, body)
  await homePage.go()
  expect(await homePage.hasPostWithSubject(subject)).toBe(true)

  await communityPage.go()
  await communityPage.leave()

  await homePage.go()
  expect(await homePage.hasPostWithSubject(subject)).toBe(false)
})

