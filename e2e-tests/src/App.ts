import { expect, type Page } from "@playwright/test"
import { AccountMenu } from "./AccountMenu"

export class App {
  private readonly page: Page
  public accountMenu: AccountMenu


  constructor(page: Page) {
    this.page = page
    this.accountMenu = new AccountMenu(page)
  }

  async launch() {
    await this.page.goto("/")
    await this.awaitAppPage()
  }

  async login(username: string = "test1@example.com", password: string = "test1") {
    await this.accountMenu.open()
    await this.accountMenu.clickSignIn()
    await this.page.getByRole("textbox", { name: "Email" }).fill(username)
    await this.page.getByRole("textbox", { name: "Password" }).fill(password)
    await this.page.getByRole("checkbox", { name: "Remember me" }).check()
    await this.page.getByRole("button", { name: "Sign In" }).click()
    await this.awaitAppPage()
    return this
  }

  async awaitAppPage() {
    await this.page.getByAltText("Spexity logo").waitFor({ state: "visible" });
  }

}