import { expect, type Page } from "@playwright/test"

export class CommunityFormPage {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async awaitPageLoad() {
    await expect(this.getNameInput()).toBeVisible()
  }

  getNameInput() {
    return this.page.locator("#name")
  }

  getTermsCheckbox() {
    return this.page.locator("#acceptTermsAndConditions")
  }

  getSubmitButton() {
    return this.page.getByRole("button", { name: "Start Community" })
  }

  async createCommunity(name: string) {
    await this.getNameInput().fill(name)
    await this.getTermsCheckbox().check()
    await this.getSubmitButton().click()
    await this.page.waitForURL("**/communities/*-*")
  }
}
