import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"
import type { CommunityPreview } from "$lib/model/types"

interface PageData {
  community: CommunityPreview
}

export const load: PageServerLoad = async (event) => {
  const { url } = event
  const communityId = url.searchParams.get("communityId")
  if (!communityId) {
    error(404)
  }
  //TODO: this shouldn't load the community data. we just need the name?
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(
    event,
    `/api/web/communities/${communityId}`,
  )
  if (!data) {
    error(404)
  }
  return data
}
