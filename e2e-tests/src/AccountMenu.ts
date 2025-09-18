import { type Page } from "@playwright/test"

export class AccountMenu {
  private readonly page: Page


  constructor(page: Page) {
    this.page = page
  }

  async open() {
    await this.page.getByRole("button", {
      name: "Account menu",
    }).click()
  }

  async clickSignIn() {
    await this.page.getByRole("link", { name: "Sign In" }).click()
  }

  async contributorHandle(): Promise<string | null> {
    return await this.page.getByRole('link', { name: 'Account profile' }).innerText()
  }

}