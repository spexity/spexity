import { expect, type Page } from "@playwright/test"
import { CommunityPage } from "./CommunityPage"
import { CommunityFormPage } from "./CommunityFormPage"

export class CommunitiesPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async go() {
    await this.page.goto("/communities")
    await this.awaitPageLoad()
  }

  async awaitPageLoad() {
    await expect(this.getCommunitiesList()).toHaveCount(1)
  }

  private getCommunitiesList() {
    return this.page.getByTestId("communities-list")
  }

  getStartCommunityButton() {
    return this.page.getByTestId("start-community-button")
  }

  async clickStartCommunity(): Promise<CommunityFormPage> {
    await this.getStartCommunityButton().click()
    await this.page.waitForURL("**/communities/new")
    const communityFormPage = new CommunityFormPage(this.page)
    await communityFormPage.awaitPageLoad()
    return communityFormPage
  }

  async createCommunity(communityName: string) {
    const communityFormPage = await this.clickStartCommunity()
    await communityFormPage.createCommunity(communityName)
    return new CommunityPage(this.page)
  }
}
