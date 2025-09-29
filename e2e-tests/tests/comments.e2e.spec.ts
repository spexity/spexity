import { expect, test } from "@playwright/test"
import { randomUUID } from "crypto"
import { App } from "../src/App"
import type { Post } from "../src/Post"
import {
  POST_WITH_LOTS_COMMENTS,
  POST_WITH_NO_COMMENTS_EVER,
  POST_WITH_NO_COMMENTS_YET,
  UNVERIFIED_USER,
} from "../src/TestData"

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
  await postPage.getCommentWithText(commentText)
})

const createComment = async (postPage: Post, text: string) => {
  await postPage.startNewComment()
  await postPage.setNewCommentValue(text)
  await postPage.submitNewComment()
  await expect(postPage.getNewCommentEditor()).toHaveCount(0)
  return await postPage.getCommentWithText(text)
}

test("client-side validation blocks empty comment", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  const postPage = await app.goToPost(POST_WITH_NO_COMMENTS_YET)
  await postPage.startNewComment()
  await postPage.submitNewComment()
  await expect(postPage.getNewCommentError()).toBeVisible()
  await expect(postPage.getNewCommentError()).toHaveText("Please write something")
  await postPage.cancelNewComment()
})

test("client-side validation blocks whitespace-only comment", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  const postPage = await app.goToPost(POST_WITH_NO_COMMENTS_YET)
  await postPage.startNewComment()
  await postPage.setNewCommentValue("   ")
  await postPage.submitNewComment()
  await expect(postPage.getNewCommentError()).toHaveText("Please write something")
  await postPage.cancelNewComment()
})

test("comments display in chronological order (oldest first)", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  const postPage = await app.goToPost(POST_WITH_LOTS_COMMENTS)
  const texts = await postPage.getVisibleCommentTexts()
  const numbers = texts
    .map((text) => {
      const match = text.match(/Seed comment (\d+)/)
      return match ? Number(match[1]) : Number.NaN
    })
    .filter((value) => !Number.isNaN(value))
  expect(numbers.length).toBeGreaterThan(1)
  const sorted = [...numbers].sort((a, b) => a - b)
  expect(numbers).toEqual(sorted)
})

test("edited comment shows edited badge", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  const postPage = await app.goToPost(POST_WITH_LOTS_COMMENTS)
  const baseText = `Editable comment ${randomUUID()}`
  const comment = await createComment(postPage, baseText)
  await comment.openEdit()
  const updateText = ` Updated`
  await comment.appendToEditor(updateText)
  await comment.saveEdit()
  await expect(comment.getEditedBadge()).toBeVisible()
  await expect(comment.getBody()).toContainText(baseText + updateText)
})

test("deleted comment shows deleted placeholder", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  const postPage = await app.goToPost(POST_WITH_LOTS_COMMENTS)
  const commentText = `Disposable comment ${randomUUID()}`
  const comment = await createComment(postPage, commentText)
  await comment.delete()
  await expect(comment.getDeletedPlaceholder()).toBeVisible()
  await expect(comment.getBody()).toHaveCount(0)
})

test("comments count updates correctly on comment added", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  const postPage = await app.goToPost(POST_WITH_NO_COMMENTS_YET)
  const before = await postPage.getCommentsCountValue()
  const commentText = `Counted comment ${randomUUID()}`
  const comment = await createComment(postPage, commentText)
  const after = await postPage.getCommentsCountValue()
  expect(after).toBeGreaterThan(before)
  await comment.delete()
  await expect(comment.getDeletedPlaceholder()).toBeVisible()
})

test("pagination loads more comments", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  const postPage = await app.goToPost(POST_WITH_LOTS_COMMENTS)
  const initialVisible = await postPage.getVisibleCommentCount()
  await postPage.loadMoreComments()
  const afterVisible = await postPage.getVisibleCommentCount()
  expect(afterVisible).toBeGreaterThan(initialVisible)
})

test("maximum 2 edits per comment", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn()
  const postPage = await app.goToPost(POST_WITH_LOTS_COMMENTS)
  const baseText = `Edit limit comment ${randomUUID()}`
  const comment = await createComment(postPage, baseText)

  await comment.openEdit()
  await comment.appendToEditor("1")
  await comment.saveEdit()

  await comment.openEdit()
  await comment.appendToEditor("2")
  await comment.saveEdit()

  await comment.openEdit()
  await comment.appendToEditor("3")
  await comment.saveEditExpectingError()
  await expect(comment.getEditError()).toHaveText("There was a problem posting your comment.")
  await comment.cancelEdit()
})

test("not logged in users cannot comment", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  const postPage = await app.goToPost(POST_WITH_NO_COMMENTS_EVER)
  await postPage.startNewComment("gated")
})

test("not verified users cannot comment", async ({ page }) => {
  const app = new App(page)
  await app.launch()
  await app.signIn(UNVERIFIED_USER)
  const postPage = await app.goToPost(POST_WITH_NO_COMMENTS_EVER)
  await postPage.startNewComment("gated")
})
