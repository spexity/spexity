import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"
import type { CommentPage, PostView } from "$lib/model/types"
import { Cookies } from "$lib/cookies"

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
  const commentsOrder = event.cookies.get(Cookies.commentsOrder) ?? "asc"
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(
    event,
    `/api/web/posts/${postId}?order=${commentsOrder}`,
  )
  if (!data) {
    error(404)
  }
  return data
}
