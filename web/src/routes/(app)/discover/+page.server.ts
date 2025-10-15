import type { PageServerLoad } from "./$types"
import type { PostPreview } from "$lib/model/types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"
import { Cookies } from "$lib/cookies"
import { redirect } from "@sveltejs/kit"

type PageData = {
  posts: PostPreview[]
}

const EMPTY: PageData = { posts: [] }

export const load: PageServerLoad<PageData> = async (event) => {
  if (!event.cookies.get(Cookies.accessToken)) {
    throw redirect(302, "/")
  }
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(event, "/api/web/discover")
  return data ?? EMPTY
}
