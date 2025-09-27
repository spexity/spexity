import test, { expect, type Page } from "@playwright/test"
import { AccountMenu } from "./AccountMenu"
import { LanguageModal } from "./LanguageModal"

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

  async signIn(username: string = "god@example.com", password: string = "god") {
    await test.step("Sign in", async () => {
      await this.accountMenu.open()
      await this.accountMenu.clickSignIn()
      await this.page.getByRole("textbox", { name: "Email" }).fill(username)
      await this.page.getByRole("textbox", { name: "Password" }).fill(password)
      await this.page.getByRole("checkbox", { name: "Remember me" }).check()
      await this.page.getByRole("button", { name: "Sign In" }).click()
      await this.awaitAppPage()
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
    await expect(this.getAuthInitDiv()).toHaveCount(0)
  }

  getLogo() {
    return this.page.getByTestId("brand-logo")
  }

  getAuthInitDiv() {
    return this.page.getByTestId("auth-init-in-progress")
  }

  async changeLanguage(locale: "en" | "zh-cn" | "ar") {
    await test.step(`Change language to ${locale}`, async () => {
      await this.accountMenu.open()
      await this.languageModal.openViaAccountMenu()
      await this.languageModal.chooseLocale(locale)
    })
  }

}