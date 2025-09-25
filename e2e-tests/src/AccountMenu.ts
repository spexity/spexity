import { type Page } from "@playwright/test"

export class AccountMenu {
  private readonly page: Page


  constructor(page: Page) {
    this.page = page
  }

  async open() {
    const btn = this.getMenuButton()
    await btn.waitFor({ state: "visible" })
    // Use keyboard interaction to avoid pointer interception by surrounding container
    await btn.focus()
    await btn.press("Enter")
    await this.getMenuContent().waitFor({ state: "visible" })
  }

  async close() {
    const content = this.getMenuContent()
    // Try clicking outside to close
    try {
      await this.page.mouse.click(5, 5)
    } catch {}
    // Fallback: press Escape
    try {
      await this.page.keyboard.press("Escape")
    } catch {}
    // Best effort small delay; don't block on visibility as DaisyUI dropdown may not toggle aria-hidden
    await this.page.waitForTimeout(100)
  }

  async isLoggedInMenu(): Promise<boolean> {
    return await this.getSignOutLink().isVisible().catch(() => false)
  }

  async clickSignIn() {
    const signInLink = this.getSignInLink()
    await signInLink.waitFor({ state: "visible" })
    // If a spinner exists, wait for it to finish, otherwise continue
    const spinner = this.getMenuButton().locator(".loading.loading-spinner")
    if (await spinner.count()) {
      await spinner.first().waitFor({ state: "detached" })
    }
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
    return this.page.getByRole("link", { name: "Sign In" })
  }

  getSignOutLink() {
    return this.page.getByRole("link", { name: "Sign Out" })
  }

  getMenuButton() {
    return this.page.getByTestId("account-menu-button")
  }

  getMenuContent() {
    return this.page.getByTestId("account-menu-content")
  }

}
