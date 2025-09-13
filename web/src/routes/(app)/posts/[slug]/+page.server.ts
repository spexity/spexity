import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"

export const load: PageServerLoad = ({ params }) => {
  if (!params || !params.slug) {
    error(404)
  }
  const postId = params.slug

  return {
    id: postId,
  }
}
