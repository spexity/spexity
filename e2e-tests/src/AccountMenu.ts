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

  async close() {
    await this.getMenuButton().blur()
  }

  async isLoggedInMenu(): Promise<boolean> {
    return await this.getSignOutLink().isVisible().catch(() => false)
  }

  async clickSignIn() {
    const signInLink = this.getSignInLink()
    await signInLink.waitFor({ state: "visible" })
    await signInLink.click()
  }

  async clickSignOut() {
    const signOutLink = this.getSignOutLink()
    await signOutLink.waitFor({ state: "visible" })
    await signOutLink.click()
  }

  async contributorHandle(): Promise<string | null> {
    return await this.page.getByRole("link", { name: "Account profile" }).innerText()
  }

  getSignInLink() {
    return this.page.getByTestId("sign-in-link")
  }

  getSignOutLink() {
    return this.page.getByTestId("sign-out-link")
  }

  getMenuButton() {
    return this.page.getByTestId("account-menu-button")
  }

  getMenuContent() {
    return this.page.getByTestId("account-menu-content")
  }

}
