import type { PageServerLoad } from "./$types"
import { env } from "$env/dynamic/private"
import type { PostPreview } from "$lib/components/PostPreview"
import { Cookies } from "$lib/cookies"
import type { ServerLoadEvent } from "@sveltejs/kit"

type PageData = {
  posts: PostPreview[]
}

export const load: PageServerLoad<PageData> = async (event) => {
  const accessToken = event.cookies.get(Cookies.accessToken)
  const data = await getPageData(event, accessToken)
  if (data == null && accessToken) {
    return (await getPageData(event)) ?? { posts: [] }
  }
  return data ?? { posts: [] }
}

const getPageData = async (event: ServerLoadEvent, accessToken?: string) => {
  try {
    const response = await event.fetch(`${env.API_URL}/api/web/topics/data`, {
      headers: accessToken
        ? {
            Authorization: `Bearer ${accessToken}`,
          }
        : undefined,
    })
    if (response.ok) {
      return (await response.json()) as PageData
    }
  } catch (err) {
    console.log("Error fetching data", err)
  }
  return null
}
