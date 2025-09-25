import { expect, type Page } from "@playwright/test"

export class LanguageModal {
  private readonly page: Page

  constructor(page: Page) {
    this.page = page
  }

  async openViaAccountMenu() {
    const languageLink = this.page.getByTestId("account-language-link")
    await expect(languageLink).toBeVisible()
    await languageLink.click()
    // Try to detect the dialog opening quickly; if not, force open via JS
    const dlg = this.page.locator('dialog.modal[open]')
    try {
      await expect(dlg).toBeVisible({ timeout: 2000 })
    } catch {
      await this.page.evaluate(() => {
        const d = document.querySelector('dialog.modal') as HTMLDialogElement | null
        d?.showModal()
      })
      await expect(dlg).toBeVisible()
    }
  }

  async waitForOpen() {
    // Wait until the native dialog is open and visible
    const dlg = this.page.locator('dialog.modal[open]')
    await expect(dlg).toBeVisible()
    await expect(dlg.locator('.modal-box')).toBeVisible()
  }

  async chooseLocale(localeTestId: string) {
    // Scope interaction to the open dialog to avoid hidden duplicates in DOM
    const dlg = this.page.locator('dialog.modal[open]')
    const btn = dlg.getByTestId(`locale-btn-${localeTestId}`)
    await expect(btn).toBeVisible()
    await btn.click()
    // Ensure dialog closes after selection (DaisyUI closes dialog when a button inside is clicked)
    await this.page.locator('dialog.modal[open]').waitFor({ state: 'detached' })
  }
}
