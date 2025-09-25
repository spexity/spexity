import { expect, type Page } from "@playwright/test"

export class SignInPage {
  constructor(private readonly page: Page) {}

  async waitForRedirectAndReturn(username: string, password: string) {
    const initialUrl = this.page.url()
    // Try to observe a navigation away from the initial URL or to the callback; don't fail if it doesn't happen (e.g., SSO already active)
    try {
      await this.page.waitForURL(u => u.href !== initialUrl || /\/auth\/login-redirect/.test(u.pathname), { timeout: 5000 })
    } catch {}

    if (this.isOnIdentityProvider()) {
      await this.completeIdentityProviderLogin(username, password)
      // After submitting at IdP, wait until callback route loads (best effort)
      try { await this.page.waitForURL(/\/auth\/login-redirect/) } catch {}
    }

    // Wait for app shell to be ready (best effort)
    try { await this.page.waitForLoadState('networkidle') } catch {}
    try { await this.page.getByTestId('brand-logo').waitFor({ state: 'visible', timeout: 20000 }) } catch {}
  }

  private isOnIdentityProvider(): boolean {
    const url = new URL(this.page.url())
    return /\/realms\/.+\/protocol\/openid-connect\//.test(url.pathname)
  }

  private async completeIdentityProviderLogin(username: string, password: string) {
    // Support common Keycloak selectors; fall back to generic
    const userField = this.page.locator('#username, input[name="username"], input[name="email"], input#email').first()
    const passField = this.page.locator('#password, input[name="password"]').first()
    await expect(userField).toBeVisible()
    await userField.fill(username)
    await expect(passField).toBeVisible()
    await passField.fill(password)
    const submit = this.page.locator('#kc-login, input[type="submit"][name="login"], button[type="submit"]').first()
    await expect(submit).toBeEnabled()
    await submit.click()
  }
}
