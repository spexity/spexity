import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"
import type { PostView } from "$lib/model/types"

interface PageData {
  post: PostView
}

export const load: PageServerLoad = async (event) => {
  const { params } = event
  if (!params || !params.slug) {
    error(404)
  }
  const postId = params.slug
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(event, `/api/web/posts/${postId}`)
  if (!data) {
    error(404)
  }
  return data
}
