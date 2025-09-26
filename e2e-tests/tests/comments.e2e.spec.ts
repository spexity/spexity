import { expect, test, type Locator } from "@playwright/test"
import { App } from "../src/App"

const POST_ID = "8032dd4e-1abd-434d-b92c-7c39f8ca359d"

test.describe("Comments flow", () => {
  test("user can create, edit, delete, and paginate comments", async ({ page }) => {
    const app = new App(page)
    await app.launch()
    await app.signIn()

    await page.goto(`/posts/${POST_ID}`)
    const countLocator = page.getByTestId("comments-count")
    await expect(countLocator).toBeVisible()
    const initialCount = await getCount(countLocator)

    const newCommentText = `Playwright comment ${Date.now()}`
    await page.getByTestId("comment-toggle").click()
    const editor = page.getByTestId("comment-editor")
    await editor.focus()
    await editor.fill(newCommentText)
    await page.getByTestId("comment-submit").click()

    const createdComment = page.locator('[data-testid^="comment-item-"]').last()
    await expect(createdComment).toContainText(newCommentText)
    const createdCommentTestId = await createdComment.getAttribute("data-testid")
    if (!createdCommentTestId) {
      throw new Error("Comment test id not found")
    }
    const createdCommentId = createdCommentTestId.replace("comment-item-", "")

    await expect(countLocator).toHaveAttribute("data-count", String(initialCount + 1))

    await page.reload()
    const persistedComment = page.getByTestId(`comment-item-${createdCommentId}`)
    await expect(persistedComment).toContainText(newCommentText)

    await page.getByTestId("comment-toggle").click()
    await page.getByTestId("comment-submit").click()
    const error = page.getByTestId("comment-error")
    await expect(error).toBeVisible()

    await page.getByTestId(`comment-edit-${createdCommentId}`).click()
    const editEditor = page.getByTestId(`comment-edit-editor-${createdCommentId}`)
    await editEditor.fill(`${newCommentText} (edit 1)`)
    await page.getByTestId(`comment-save-${createdCommentId}`).click()
    await expect(page.getByTestId(`comment-item-${createdCommentId}`)).toContainText("edit 1")
    await expect(page.getByTestId(`comment-edited-badge-${createdCommentId}`)).toBeVisible()

    await page.getByTestId(`comment-edit-${createdCommentId}`).click()
    await editEditor.fill(`${newCommentText} (edit 2)`)
    await page.getByTestId(`comment-save-${createdCommentId}`).click()
    await expect(page.getByTestId(`comment-item-${createdCommentId}`)).toContainText("edit 2")

    await page.getByTestId(`comment-delete-${createdCommentId}`).click()
    await page.getByTestId(`comment-delete-confirm-${createdCommentId}`).click()
    await expect(page.getByTestId(`comment-item-${createdCommentId}`)).toContainText("Comment deleted by author")
    await expect(countLocator).toHaveAttribute("data-count", String(initialCount))

    await page.goto("/")
    const homeCount = page.getByTestId(`post-preview-comments-count-${POST_ID}`)
    await expect(homeCount).toHaveAttribute("data-count", String(initialCount))

    await page.goto(`/posts/${POST_ID}`)
    const loadMoreButton = page.getByTestId("comments-load-more")
    await expect(loadMoreButton).toBeVisible()
    const beforeLoadCount = await page.locator('[data-testid^="comment-item-"]').count()
    await loadMoreButton.focus()
    await page.keyboard.press("Enter")
    await expect(loadMoreButton).toHaveAttribute("aria-busy", "true")
    await expect.poll(async () => {
      return await page.locator('[data-testid^="comment-item-"]').count()
    }).toBeGreaterThan(beforeLoadCount)
    await expect(loadMoreButton).not.toBeVisible()
  })
})

async function getCount(locator: Locator): Promise<number> {
  const attr = await locator.getAttribute("data-count")
  return attr ? Number(attr) : 0
}
