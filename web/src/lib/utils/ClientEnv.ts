import { Cookies } from "$lib/cookies"

export class ClientEnv {
  static setup() {
    const tz = this.timezone()
    document.cookie = `${Cookies.timezone}=${encodeURIComponent(tz)}; path=/; max-age=315360000`
  }

  static timezone() {
    return Intl.DateTimeFormat().resolvedOptions().timeZone
  }
}
