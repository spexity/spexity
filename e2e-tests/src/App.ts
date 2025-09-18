import { expect, type Page } from "@playwright/test"
import { AccountMenu } from "./AccountMenu"

export class App {
  private readonly page: Page
  private accountMenuObj: AccountMenu | undefined


  constructor(page: Page) {
    this.page = page
  }

  async launch() {
    await this.page.goto("/")
    await expect(this.page.getByAltText("Spexity logo")).toBeVisible()
  }

  async login(username: string = "test1@example.com", password: string = "test1") {
    const accountMenu = await this.accountMenu()
    await accountMenu.clickSignIn()
    await this.page.getByRole("textbox", { name: "Email" }).fill(username)
    await this.page.getByRole("textbox", { name: "Password" }).fill(password)
    await this.page.getByRole("checkbox", { name: "Remember me" }).check()
    await this.page.getByRole("button", { name: "Sign In" }).click()
    await expect(this.page.getByAltText("Spexity logo")).toBeVisible()
    return this
  }

  async accountMenu(): Promise<AccountMenu> {
    if (!this.accountMenuObj) {
      this.accountMenuObj = new AccountMenu(this.page)
    }
    await this.accountMenuObj.open()
    return this.accountMenuObj
  }

}