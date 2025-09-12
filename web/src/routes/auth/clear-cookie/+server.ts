import type { RequestHandler } from "@sveltejs/kit"
import { ACCESS_TOKEN_COOKIE } from "$lib/auth-constants"

export const POST: RequestHandler = async ({ cookies }) => {
  cookies.delete(ACCESS_TOKEN_COOKIE, { path: "/" })
  return new Response(null, { status: 204 })
}
