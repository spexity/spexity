import { expect, test } from "@playwright/test"
import { App } from "../src/App"

test.describe("Accessibility Contract Tests", () => {

  test("button label + aria-label localized in all locales", async ({ page }) => {
    const app = new App(page)
    await app.launch()
    await expect(app.accountMenu.getMenuButton()).toHaveAttribute("aria-label", "Account menu button")
    await app.changeLanguage("ar")
    await expect(app.accountMenu.getMenuButton()).toHaveAttribute("aria-label", "زر قائمة الحساب")
    await app.changeLanguage("zh-cn")
    await expect(app.accountMenu.getMenuButton()).toHaveAttribute("aria-label", "帐户菜单按钮")
  })

})
