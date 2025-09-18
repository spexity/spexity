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
    const signInLink = this.page.getByRole("link", { name: "Sign In" });
    await signInLink.waitFor({ state: "visible" });
    const accountMenu = this.page.getByRole("button", { name: "Account menu" })
    await accountMenu.locator(".loading.loading-spinner").waitFor({ state: "detached" })
    await signInLink.click()
  }

  async contributorHandle(): Promise<string | null> {
    return await this.page.getByRole('link', { name: 'Account profile' }).innerText()
  }

}
