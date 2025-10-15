import { expect, type Page } from "@playwright/test"

export class HomePage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async go() {
    await this.page.goto("/")
    await this.awaitPageLoad()
  }

  async awaitPageLoad() {
    await this.getLogo().waitFor({ state: "visible" })
    await expect(this.getPostsList()).toHaveCount(1)
    await expect(this.getInitDiv()).toHaveCount(0)
  }

  async hasPostWithSubject(subject: string): Promise<boolean> {
    const posts = await this.page.getByTestId("post-subject").allTextContents()
    return posts.includes(subject)
  }

  getLogo() {
    return this.page.getByTestId("brand-logo")
  }

  getPostsList() {
    return this.page.getByTestId("posts-list")
  }

  getInitDiv() {
    return this.page.getByTestId("init-in-progress")
  }
}
