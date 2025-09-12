import { error } from "@sveltejs/kit"
import type { PageServerLoad } from "./$types"

export const load: PageServerLoad = ({ params }) => {
  if (!params || !params.slug) {
    error(404)
  }
  return {
    community: {
      name: "Toyota 86",
    },
  }
}
