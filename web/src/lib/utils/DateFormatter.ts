const MINUTE_MS = 60 * 1000
const HOUR_MS = 60 * MINUTE_MS
const DAY_MS = 24 * HOUR_MS
const WEEK_MS = 7 * DAY_MS
const YEAR_MS = 365 * DAY_MS
const TIME_ONLY_CUTOFF_MS = 18 * HOUR_MS
const DATE_TIME_CUTOFF_MS = WEEK_MS

export class DateFormatter {
  //Format from an iso string to a relative time representation (as opposed to a specific date or time)
  static formatUtcIsoRelative(utcIso: string): string {
    const rtf = new Intl.RelativeTimeFormat(undefined, { numeric: "auto" })

    const date = new Date(utcIso)
    const now = Date.now()
    const diffMs = now - date.getTime()
    if (diffMs <= MINUTE_MS) {
      return rtf.format(0, "second")
    } else if (diffMs < HOUR_MS) {
      const diffMinutes = Math.round(diffMs / MINUTE_MS)
      return rtf.format(diffMinutes, "minute")
    } else if (diffMs < DAY_MS) {
      const diffHours = Math.round(diffMs / HOUR_MS)
      return rtf.format(diffHours, "hour")
    } else if (diffMs < 4 * WEEK_MS) {
      const diffDays = Math.round(diffMs / DAY_MS)
      return rtf.format(diffDays, "day")
    } else if (diffMs < 52 * WEEK_MS) {
      const diffWeeks = Math.round(diffMs / WEEK_MS)
      return rtf.format(diffWeeks, "week")
    }
    const diffYears = Math.max(1, Math.round(diffMs / YEAR_MS))
    return rtf.format(diffYears, "year")
  }

  //Format from an iso string to an absolute representation (at a specific date or time, as opposed to 1d ago)
  static formatUtcIsoAbsolute(utcIso: string, timezone: string, locale: string): string {
    const date = new Date(utcIso)
    const now = Date.now()
    const diffMs = now - date.getTime()
    if (diffMs <= TIME_ONLY_CUTOFF_MS) {
      return date.toLocaleTimeString(locale, {
        timeStyle: "short",
        timeZone: timezone,
      })
    }
    if (diffMs <= DATE_TIME_CUTOFF_MS) {
      return date.toLocaleString(locale, {
        dateStyle: "medium",
        timeStyle: "short",
        timeZone: timezone,
      })
    }
    return date.toLocaleDateString(locale, {
      dateStyle: "medium",
      timeZone: timezone,
    })
  }
}
