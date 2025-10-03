import type { OrderPref, Prefs } from "$lib/model/types"
import { LOCALES } from "$lib/locales"
import { Cookies } from "$lib/cookies"

export class PrefsManager {
  timezone: string = $state("UTC")
  locale: string = $state(LOCALES[0].id)
  commentsOrder: OrderPref = $state("asc")

  set(prefs: Prefs) {
    this.timezone = prefs.timezone
    this.locale = prefs.locale
    this.commentsOrder = prefs.commentsOrder
  }

  setCommentsOrder(val: OrderPref) {
    if (val === "desc") {
      document.cookie = `${Cookies.commentsOrder}=${val}; path=/; max-age=315360000`
      this.commentsOrder = "desc"
    } else {
      document.cookie = `${Cookies.commentsOrder}=; path=/; max-age=0`
      this.commentsOrder = "asc"
    }
  }
}
