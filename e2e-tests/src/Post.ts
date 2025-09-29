import { expect, type Locator, type Page } from "@playwright/test"
import { Comment } from "./Comment"

export class Post {
  private readonly page: Page
  private readonly id: string

  constructor(page: Page, id: string) {
    this.page = page
    this.id = id
  }

  async go() {
    await this.page.goto(`/posts/${this.id}`)
    await expect(this.getPostTitle()).toBeVisible()
    await expect(this.getNewCommentButton()).toBeVisible()
  }

  getPostTitle() {
    return this.page.getByTestId("post-subject")
  }

  async startNewComment(expectation?: "works" | "gated") {
    const toggleButton = this.getNewCommentButton()
    await toggleButton.scrollIntoViewIfNeeded()
    await expect(toggleButton).toBeVisible()
    await toggleButton.click()
    const expectationLocator = expectation === "gated" ? this.getGatedFeature() : this.getNewCommentEditor()
    try {
      await expect(expectationLocator).toBeVisible({ timeout: 1000 });
    } catch {
      await toggleButton.click();
      await expect(expectationLocator).toBeVisible();
    }
  }

  private getNewCommentButton() {
    return this.page.getByTestId("new-comment-button")
  }

  async setNewCommentValue(value: string) {
    const editor = this.getNewCommentEditor()
    await expect(editor).toBeVisible()
    await editor.click()
    await this.page.keyboard.type(value)
  }

  async getCommentWithText(value: string) {
    const matching = this.getCommentItems().filter({
      has: this.page.locator("[data-testid^=\"comment-body-\"]").filter({ hasText: value }),
    })
    const item = matching.first()
    await expect(item).toBeVisible()
    const testId = await item.getAttribute("data-testid")
    if (!testId) {
      throw new Error("Comment test id not found")
    }
    const stable = this.page.getByTestId(testId)
    await expect(stable).toBeVisible()
    return new Comment(this.page, stable)
  }

  async submitNewComment() {
    await this.getNewCommentSubmitButton().click()
  }

  getCommentItems() {
    return this.page.locator("[data-testid^=\"comment-item-\"]")
  }

  getNewCommentEditor() {
    return this.page.getByTestId("new-comment-editor")
  }

  getNewCommentSubmitButton() {
    return this.page.getByTestId("new-comment-submit")
  }

  getCommentsCount() {
    return this.page.getByTestId("comments-count")
  }

  async getCommentsCountText(): Promise<string> {
    const element = this.getCommentsCount()
    return await element.innerText()
  }

  async getCommentsCountValue(): Promise<number> {
    const text = await this.getCommentsCountText()
    const match = text.match(/\d+/)
    return match ? Number(match[0]) : 0
  }

  getNewCommentError() {
    return this.page.getByTestId("new-comment-error")
  }

  getNewCommentCancelButton() {
    return this.page.getByTestId("new-comment-cancel")
  }

  async cancelNewComment() {
    const cancelButton = this.getNewCommentCancelButton()
    if (await cancelButton.count()) {
      await cancelButton.click()
    }
  }

  getGatedFeature() {
    return this.page.getByTestId("gated-feature")
  }

  getCommentBodies(): Locator {
    return this.page.locator("[data-testid^=\"comment-body-\"]")
  }

  async getVisibleCommentTexts(): Promise<string[]> {
    const contents = await this.getCommentBodies().allInnerTexts()
    return contents.map((text) => text.trim()).filter((text) => text.length > 0)
  }

  async getVisibleCommentCount(): Promise<number> {
    return await this.getCommentItems().count()
  }

  getLoadMoreButton() {
    return this.page.getByTestId("comments-load-more")
  }

  async loadMoreComments() {
    const button = this.getLoadMoreButton()
    if (!(await button.count())) {
      return
    }
    await button.scrollIntoViewIfNeeded()
    await expect(button).toBeEnabled()
    await button.click()
    await this.page.getByTestId("comments-loading-more").waitFor({ state: 'detached' })
  }

}
