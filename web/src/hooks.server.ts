import type { Handle } from "@sveltejs/kit"
import { paraglideMiddleware } from "$lib/paraglide/server"
import { sequence } from "@sveltejs/kit/hooks"
import { Cookies } from "$lib/cookies"

const handleParaglide: Handle = ({ event, resolve }) =>
  paraglideMiddleware(event.request, ({ request, locale }) => {
    event.request = request
    event.locals.locale = locale
    return resolve(event, {
      transformPageChunk: ({ html }) => html.replace("%paraglide.lang%", locale),
    })
  })

const handleLocals: Handle = ({ event, resolve }) => {
  event.locals.timezone = event.cookies.get(Cookies.timezone) ?? "UTC"
  return resolve(event)
}

export const handle: Handle = sequence(handleParaglide, handleLocals)
