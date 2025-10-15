import test, { type Page } from "@playwright/test"
import { AccountMenu } from "./AccountMenu"
import { LanguageModal } from "./LanguageModal"
import { GOD_USER, type UserCredentials } from "./TestData"
import { HomePage } from "./HomePage"
import { Post } from "./Post"
import { CommunitiesPage } from "./CommunitiesPage"

export class App {
  private readonly page: Page
  public homePage: HomePage
  public communitiesPage: CommunitiesPage
  public accountMenu: AccountMenu
  public languageModal: LanguageModal


  constructor(page: Page) {
    this.page = page
    this.accountMenu = new AccountMenu(page)
    this.languageModal = new LanguageModal(page)
    this.homePage = new HomePage(page)
    this.communitiesPage = new CommunitiesPage(page)
  }

  async launch() {
    return this.goToHome()
  }

  async goToPost(id: string) {
    let post = new Post(this.page, id)
    await post.go()
    return post
  }

  async goToHome() {
    await this.homePage.go()
    return this.homePage;
  }

  async goToCommunities() {
    await this.communitiesPage.go()
    return this.communitiesPage
  }

  async signIn(credentials: UserCredentials = GOD_USER) {
    await test.step("Sign in", async () => {
      await this.accountMenu.open()
      await this.accountMenu.clickSignIn()
      await this.page.getByRole("textbox", { name: "Email" }).fill(credentials.username)
      await this.page.getByRole("textbox", { name: "Password" }).fill(credentials.password)
      await this.page.getByRole("checkbox", { name: "Remember me" }).check()
      await this.page.getByRole("button", { name: "Sign In" }).click()
      await this.homePage.awaitPageLoad()
    })
  }

  async signOut() {
    await test.step("Sign out", async () => {
      await this.accountMenu.open()
      await this.accountMenu.clickSignOut()
      await this.page.waitForTimeout(250)
      await this.homePage.awaitPageLoad()
    })
  }

  async changeLanguage(locale: "en" | "zh-cn" | "ar") {
    await test.step(`Change language to ${locale}`, async () => {
      await this.accountMenu.open()
      await this.languageModal.openViaAccountMenu()
      await this.languageModal.chooseLocale(locale)
    })
  }

}