import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"
import type { CommunityPreview, CommunityPreviewPost } from "$lib/model/types"

interface PageData {
  community: CommunityPreview
  posts: CommunityPreviewPost[]
}

export const load: PageServerLoad = async (event) => {
  const { url } = event
  const communityId = url.searchParams.get("communityId")
  if (!communityId) {
    error(404)
  }
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(
    event,
    `/api/web/communities/${communityId}`,
  )
  if (!data) {
    error(404)
  }
  return data
}
