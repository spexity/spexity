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
    await expect(this.page.getByTestId("communities-list")).toBeVisible()
    return this
  }

  getStartCommunityButton() {
    return this.page.getByTestId("start-community-button")
  }

  async clickStartCommunity(): Promise<CommunityFormPage> {
    await this.getStartCommunityButton().click()
    await this.page.waitForURL("**/communities/new")
    const communityFormPage = new CommunityFormPage(this.page)
    await communityFormPage.waitForPageLoad()
    return communityFormPage
  }

  getCommunitiesList() {
    return this.page.getByTestId("communities-list")
  }

  async openFirstCommunity(): Promise<CommunityPage> {
    const firstCommunityLink = this.getCommunitiesList().locator("a.btn").first()
    await expect(firstCommunityLink).toBeVisible()
    const href = await firstCommunityLink.getAttribute("href")
    if (!href) throw new Error("Could not find community link")
    await firstCommunityLink.click()
    await this.page.waitForURL("**/communities/*")
    const communityPage = new CommunityPage(this.page)
    await communityPage.waitForPageLoad()
    return communityPage
  }
}
