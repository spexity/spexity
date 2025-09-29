import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"
import type { CommentPage, PostView } from "$lib/model/types"

interface PageData {
  post: PostView
  comments: CommentPage
}

export const load: PageServerLoad<PageData> = async (event) => {
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
