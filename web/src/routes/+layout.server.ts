import type { LayoutServerLoad } from "./$types"

type PageData = {
  timezone: string
  locale: string
}

export const load: LayoutServerLoad = async (event) => {
  return {
    timezone: event.locals.timezone,
    locale: event.locals.locale,
  } as PageData
}
