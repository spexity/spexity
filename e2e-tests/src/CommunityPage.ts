import { expect, type Page } from "@playwright/test";
import { PostFormPage } from "./PostFormPage";

export class CommunityPage {
  private readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  async waitForPageLoad() {
    await expect(this.page.getByTestId("create-post-button")).toBeVisible();
  }

  getCreatePostButton() {
    return this.page.getByTestId("create-post-button");
  }

  async clickCreatePost(): Promise<PostFormPage> {
    await this.getCreatePostButton().click();
    await this.page.waitForURL("**/posts/new?*");
    const postFormPage = new PostFormPage(this.page);
    await postFormPage.waitForPageLoad();
    return postFormPage;
  }
}
