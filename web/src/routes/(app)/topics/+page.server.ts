import type { PageServerLoad } from "./$types"
import { SsrLoadFromApi } from "$lib/utils/SsrLoadFromApi"
import type { TopicPreview } from "$lib/model/types"

type PageData = {
  topics: TopicPreview[]
}

const EMPTY: PageData = { topics: [] }

export const load: PageServerLoad<PageData> = async (event) => {
  const data = await SsrLoadFromApi.loadAuthLenient<PageData>(event, "/api/web/topics")
  return data ?? EMPTY
}
