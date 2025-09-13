import type { PageServerLoad } from "./$types"
import type { PostPreview } from "$lib/components/PostPreview"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"

type PageData = {
  posts: PostPreview[]
}

const EMPTY: PageData = { posts: [] }

export const load: PageServerLoad<PageData> = async (event) => {
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(event, "/api/web/home")
  return data ?? EMPTY
}
