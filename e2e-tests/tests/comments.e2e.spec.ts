import { expect, test } from "@playwright/test"
import { App } from "../src/App"
import { POST_WITH_NO_COMMENTS_YET } from "../src/TestData"

test("verified user submit valid comment and see it appear", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  const postPage = await app.goToPost(POST_WITH_NO_COMMENTS_YET)
  await postPage.startNewComment()
  const commentText = "Hello world " + crypto.randomUUID()
  await postPage.setNewCommentValue(commentText)
  await postPage.submitNewComment()
  await expect(postPage.getNewCommentEditor()).toBeVisible({ visible: false })
  let comment = await postPage.getCommentWithText(commentText)
  let commentId = await comment.getCommentId()
  console.log("comment id ", commentId)
})
