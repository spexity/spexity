export const Cookies = {
  accessToken: "access_token",
  locale: "locale",
  timezone: "tz",
  commentsOrder: "comments_order",
  theme: "theme",
  contributorId: "contributor_id",
}

export class CookieUtils {
  static set(name: string, val: string, maxAge?: number) {
    if (typeof document != "undefined") {
      document.cookie = `${name}=${encodeURIComponent(val)}; path=/; max-age=${maxAge ?? 315360000}`
    }
  }

  static delete(name: string) {
    if (typeof document != "undefined") {
      document.cookie = `${name}=; path=/; max-age=0`
    }
  }
}
