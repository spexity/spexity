import { expect, type Page } from "@playwright/test"
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

  getPostContent() {
    return this.page.locator(".tiptap").first()
  }

  async startNewComment() {
    const toggleButton = this.getNewCommentButton()
    await toggleButton.scrollIntoViewIfNeeded()
    await expect(toggleButton).toBeVisible()
    await toggleButton.click()
    try {
      await expect(this.getNewCommentEditor()).toBeVisible({ timeout: 1000 });
    } catch {
      await toggleButton.click();
      await expect(this.getNewCommentEditor()).toBeVisible();
    }
  }

  private getNewCommentButton() {
    return this.page.getByTestId("new-comment-button")
  }

  async setNewCommentValue(value: string) {
    let editor = this.getNewCommentEditor()
    await editor.click()
    await this.page.keyboard.type(value)
  }

  async getCommentWithText(value: string) {
    const item = this.getCommentItems().filter({
      has: this.page.locator("[data-testid^=\"comment-body-\"]").filter({ hasText: value }),
    })
    return new Comment(this.page, item.first())
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

}