import { Cookies } from "$lib/cookies"
import type { SortPreference } from "$lib/model/types"

export class ClientEnv {
  static setup() {
    const tz = this.timezone()
    document.cookie = `${Cookies.timezone}=${encodeURIComponent(tz)}; path=/; max-age=315360000`
  }

  static setCommentsOrder(val: SortPreference) {
    if (val === "desc") {
      document.cookie = `${Cookies.commentsOrder}=${val}; path=/; max-age=315360000`
    } else {
      document.cookie = `${Cookies.commentsOrder}=; path=/; max-age=0`
    }
  }

  static timezone() {
    return Intl.DateTimeFormat().resolvedOptions().timeZone
  }
}
