import { Cookies, CookieUtils } from "$lib/cookies"

export class ClientEnv {
  static setup() {
    const tz = this.timezone()
    CookieUtils.set(Cookies.timezone, tz)
  }

  static timezone() {
    return Intl.DateTimeFormat().resolvedOptions().timeZone
  }
}
