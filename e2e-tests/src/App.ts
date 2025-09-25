import test, { expect, type Page } from "@playwright/test"
import { AccountMenu } from "./AccountMenu"
import { LanguageModal } from "./LanguageModal"
import { SignInPage } from "./SignInPage"

export class App {
  private readonly page: Page
  public accountMenu: AccountMenu
  public languageModal: LanguageModal


  constructor(page: Page) {
    this.page = page
    this.accountMenu = new AccountMenu(page)
    this.languageModal = new LanguageModal(page)
  }

  async launch() {
    await this.page.goto("/")
    await this.awaitAppPage()
  }

  async signIn(username: string = "test1@example.com", password: string = "test1") {
    await test.step("Sign in", async () => {
      await this.accountMenu.open()
      await this.accountMenu.clickSignIn()
      // The app uses OIDC redirect flow. Wait for callback to complete and app shell to reappear.
  const signInPage = new SignInPage(this.page)
  await signInPage.waitForRedirectAndReturn(username, password)
      // Verify account menu reflects logged-in state by checking that Sign Out is visible when opening menu again
      // Retry loop to wait for UI state in Chromium
      for (let i = 0; i < 3; i++) {
        await this.accountMenu.open()
        if (await this.accountMenu.isLoggedInMenu()) {
          break
        }
        await this.accountMenu.close()
        await this.page.waitForTimeout(300)
      }
      await expect(this.accountMenu.getSignOutLink()).toBeVisible()
    })
  }

  async signOut() {
    await test.step("Sign out", async () => {
      await this.accountMenu.open()
      await this.accountMenu.clickSignOut()
      await this.page.waitForTimeout(250)
      await this.awaitAppPage()
    })
  }

  async awaitAppPage() {
    await this.getLogo().waitFor({ state: "visible" })
  }

  getLogo() {
    return this.page.getByTestId("brand-logo")
  }

  async changeLanguage(locale: "en" | "zh-cn" | "ar") {
    await test.step(`Change language to ${locale}`, async () => {
      await this.accountMenu.open()
      await this.languageModal.openViaAccountMenu()
      await this.languageModal.chooseLocale(locale)
    })
  }

}