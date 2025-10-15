import type { OrderPref, Prefs } from "$lib/model/types"
import { LOCALES } from "$lib/locales"
import { Cookies, CookieUtils } from "$lib/cookies"
import type { Theme } from "$lib/utils/ThemeHandler"

export class PrefsManager {
  timezone: string = $state("UTC")
  locale: string = $state(LOCALES[0].id)
  commentsOrder: OrderPref = $state("asc")
  theme: Theme = $state("system")

  set(prefs: Prefs) {
    this.timezone = prefs.timezone
    this.locale = prefs.locale
    this.setCommentsOrder(prefs.commentsOrder)
    this.setTheme(prefs.theme)
  }

  setCommentsOrder(val: OrderPref) {
    if (val === "desc") {
      CookieUtils.set(Cookies.commentsOrder, val)
      this.commentsOrder = "desc"
    } else {
      CookieUtils.delete(Cookies.commentsOrder)
      this.commentsOrder = "asc"
    }
  }

  setTheme(val: Theme) {
    if (val === "system") {
      CookieUtils.delete(Cookies.theme)
      this.theme = "system"
    } else {
      CookieUtils.set(Cookies.theme, val)
      this.theme = val
    }
  }
}
