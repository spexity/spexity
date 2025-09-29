import { type Locator, type Page } from "@playwright/test"

export class Comment {
  readonly page: Page
  private locator: Locator

  constructor(page: Page, locator: Locator) {
    this.page = page
    this.locator = locator
  }

  async getCommentId() {
    const testId = await this.locator.getAttribute("data-testid")
    if (!testId) {
      throw new Error("Comment id not found")
    }
    return testId.replace("comment-item-", "")
  }

}
