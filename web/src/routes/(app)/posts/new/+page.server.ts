import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"

interface PageData {
  communityId: string
  communityName: string
}

interface PageDataResponse {
  communityName: string
}

export const load: PageServerLoad = async (event) => {
  const { url } = event
  const communityId = url.searchParams.get("communityId")
  if (!communityId) {
    error(404)
  }
  const data = await SsrLoadFromApi.loadAuthLenient<PageDataResponse>(
    event,
    `/api/web/posts/new?communityId=${communityId}`,
  )
  if (!data) {
    error(404)
  }
  return {
    communityId,
    ...data,
  } as PageData
}
