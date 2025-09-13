import type { PageServerLoad } from "./$types"
import type { CommunityPreview } from "$lib/components/CommunityPreview"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"

type PageData = {
  communities: CommunityPreview[]
}

const EMPTY: PageData = { communities: [] }

export const load: PageServerLoad<PageData> = async (event) => {
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(event, "/api/web/communities")
  return data ?? EMPTY
}
