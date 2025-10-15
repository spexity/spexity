import type { LayoutServerLoad } from "./$types"

import type { Prefs } from "$lib/model/types"

type PageData = {
  prefs: Prefs
  currentContributorId?: string
}

export const load: LayoutServerLoad = async (event) => {
  return {
    prefs: {
      timezone: event.locals.timezone,
      locale: event.locals.locale,
      commentsOrder: event.locals.commentsOrder,
      theme: event.locals.theme,
    },
    currentContributorId: event.locals.contributorId,
  } as PageData
}
