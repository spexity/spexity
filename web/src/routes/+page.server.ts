import type { PageServerLoad } from "./$types"
import { env } from "$env/dynamic/private"
import type { PostPreview } from "$lib/components/PostPreview"

type PageData = {
  posts: PostPreview[]
}

export const load: PageServerLoad<PageData> = async () => {
  const response = await fetch(`${env.API_URL}/api/web/home/data`)
  const data: PageData = await response.json()
  return data
}
