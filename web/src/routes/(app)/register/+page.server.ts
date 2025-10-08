import { redirect } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"
import { Cookies } from "$lib/cookies"

export const load: PageServerLoad = async (event) => {
  if (event.cookies.get(Cookies.accessToken)) {
    throw redirect(302, "/")
  }
  return {}
}
