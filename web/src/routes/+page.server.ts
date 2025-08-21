import type { PageServerLoad } from "./$types"

type PageData = {
  posts: Post[]
}

type Post = {
  id: string
  subject: string
  body: string
}

export const load: PageServerLoad<PageData> = async () => {
  const response = await fetch("http://localhost:8080/api/web/home/data")
  const data: PageData = await response.json()
  return data
}
