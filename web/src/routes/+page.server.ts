import type { PageServerLoad } from "./$types"
import { env } from "$env/dynamic/private"

type PageData = {
  posts: PostPreview[]
}

type PostPreview = {
  id: string
  subject: string
  body: string
  createdAt: string
  contributorHandle: string
  communityName: string
  communitySlug: string
}

export const load: PageServerLoad<PageData> = async () => {
  const response = await fetch(`${env.API_URL}/api/web/home/data`)
  const data: PageData = await response.json()
  return data
}
