import type { PageServerLoad } from "./$types"
import { env } from "$env/dynamic/private"
import type { PostPreview } from "$lib/components/PostPreview"
import { ACCESS_TOKEN_COOKIE } from "$lib/auth-constants"

type PageData = {
  posts: PostPreview[]
}

export const load: PageServerLoad<PageData> = async (event) => {
  const accessToken = event.cookies.get(ACCESS_TOKEN_COOKIE)
  const response = await event.fetch(`${env.API_URL}/api/web/home/data`, {
    headers: accessToken
      ? {
          Authorization: `Bearer ${accessToken}`,
        }
      : undefined,
  })
  const data: PageData = await response.json()
  return data
}
