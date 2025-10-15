import { expect, type Page } from "@playwright/test";
import { PostFormPage } from "./PostFormPage";

export class CommunityPage {
  private readonly page: Page;
  private readonly communityId: string

  constructor(page: Page) {
    this.page = page;
    const url = new URL(page.url());
    const pathParts = url.pathname.split("/");
    this.communityId = pathParts[pathParts.length - 1];
  }

  async awaitPageLoad() {
    await this.page.waitForURL("**/communities/*-*");
    await expect(this.getPostsList()).toBeVisible();
  }

  async go() {
    await this.page.goto(`/communities/${this.communityId}`);
    await this.awaitPageLoad();
  }

  async isJoined() {
    return await this.getLeaveButton().isVisible().catch(() => false)
  }

  async join() {
    await expect(this.getJoinButton()).toBeVisible()
    await this.getJoinButton().click()
    await expect(this.getLeaveButton()).toBeVisible()
  }

  async leave() {
    await expect(this.getLeaveButton()).toBeVisible()
    await this.getLeaveButton().click()
    await expect(this.getJoinButton()).toBeVisible()
  }

  getPostsList() {
    return this.page.getByTestId("community-posts-list");
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

  async clickCreatePost(): Promise<PostFormPage> {
    await this.getCreatePostButton().click();
    await this.page.waitForURL("**/posts/new?*");
    const postFormPage = new PostFormPage(this.page);
    await postFormPage.awaitPageLoad();
    return postFormPage;
  }

  async createPost(subject: string, body: string) {
    const postFormPage = await this.clickCreatePost()
    await postFormPage.createPost(subject, body)
  }

}
