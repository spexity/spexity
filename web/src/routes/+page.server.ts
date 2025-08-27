import type { PageServerLoad } from "./$types"
import { env } from '$env/dynamic/private';

type PageData = {
  posts: Post[]
}

type Post = {
  id: string
  subject: string
  body: string
}

export const load: PageServerLoad<PageData> = async () => {
  const response = await fetch(`${env.API_URL}/api/web/home/data`)
  const data: PageData = await response.json()
  return data
}
