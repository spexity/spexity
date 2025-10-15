import { expect, type Page } from "@playwright/test";
import { PostFormPage } from "./PostFormPage";

export class CommunityPage {
  private readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  async waitForPageLoad() {
    await expect(this.getPostsList()).toBeVisible();
  }

  async isJoined() {
    return await this.getLeaveButton().isVisible().catch(() => false)
  }

  async join() {
    await expect(this.getJoinButton()).toBeVisible()
    await this.getJoinButton().click()
    await expect(this.getLeaveButton()).toBeVisible()
  }

  getJoinButton() {
    return this.page.getByTestId("join-community-button");
  }

  getLeaveButton() {
    return this.page.getByTestId("leave-community-button");
  }

  getCreatePostButton() {
    return this.page.getByTestId("create-post-button");
  }

  getPostsList() {
    return this.page.getByTestId("posts-list");
  }

  async clickCreatePost(): Promise<PostFormPage> {
    await this.getCreatePostButton().click();
    await this.page.waitForURL("**/posts/new?*");
    const postFormPage = new PostFormPage(this.page);
    await postFormPage.waitForPageLoad();
    return postFormPage;
  }
}
