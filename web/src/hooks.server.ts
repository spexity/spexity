import type { Handle } from "@sveltejs/kit"
import { paraglideMiddleware } from "$lib/paraglide/server"
import { sequence } from "@sveltejs/kit/hooks"
import { Cookies } from "$lib/cookies"
import { LOCALES_MAP } from "$lib/locales"

const handleParaglide: Handle = ({ event, resolve }) =>
  paraglideMiddleware(event.request, ({ request, locale }) => {
    event.request = request
    event.locals.locale = locale
    return resolve(event, {
      transformPageChunk: ({ html }) =>
        html
          .replace("%paraglide.lang%", locale)
          .replace("%paraglide.langDir%", LOCALES_MAP[locale]?.rtl ? "rtl" : "ltr"),
    })
  })

const handleLocals: Handle = ({ event, resolve }) => {
  event.locals.timezone = event.cookies.get(Cookies.timezone) ?? "UTC"
  return resolve(event)
}

const handleSecurityHeaders: Handle = ({ event, resolve }) => {
  if (!event.isDataRequest) {
    event.setHeaders({
      "X-Frame-Options": "DENY",
      "Strict-Transport-Security": "max-age=31536000; includeSubDomains; preload",
      "X-Content-Type-Options": "nosniff",
      "Permissions-Policy": "geolocation=(), microphone=(), camera=(self)",
      "Referrer-Policy": "no-referrer",
      "Cross-Origin-Embedder-Policy": "require-corp",
      "Cross-Origin-Opener-Policy": "same-origin",
      "Cross-Origin-Resource-Policy": "same-origin",
    })
  }
  return resolve(event)
}

export const handle: Handle = sequence(handleParaglide, handleLocals, handleSecurityHeaders)
