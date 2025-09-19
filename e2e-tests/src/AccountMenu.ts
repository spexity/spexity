import { type Page } from "@playwright/test"

export class AccountMenu {
  private readonly page: Page


  constructor(page: Page) {
    this.page = page
  }

  async open() {
    await this.getMenuButton().click()
    await this.getMenuContent().waitFor({ state: "visible" })
  }

  async clickSignIn() {
    const signInLink = this.getSignInLink()
    await signInLink.waitFor({ state: "visible" })
    await this.getMenuButton().locator(".loading.loading-spinner").waitFor({ state: "detached" })
    await signInLink.click()
  }

  async contributorHandle(): Promise<string | null> {
    return await this.page.getByRole("link", { name: "Account profile" }).innerText()
  }

  getSignInLink() {
    return this.page.getByRole("link", { name: "Sign In" })
  }

  getMenuButton() {
    return this.page.getByRole("button", { name: "Account menu button" })
  }

  getMenuContent() {
    return this.page.getByRole("list", {
      name: "Account menu content",
    })
  }

}
