import type { RequestHandler } from "@sveltejs/kit"
import { ACCESS_TOKEN_COOKIE } from "$lib/auth-constants"

export const POST: RequestHandler = async ({ request, cookies }) => {
  const { access_token, expires_at } = await request.json().catch(() => ({}))
  if (!access_token || !expires_at) {
    return new Response("Bad Request", { status: 400 })
  }
  const maxAge = Math.max(0, Math.floor(expires_at - Date.now() / 1000))
  cookies.set(ACCESS_TOKEN_COOKIE, access_token, {
    path: "/",
    httpOnly: true,
    secure: true,
    sameSite: "lax",
    maxAge,
  })
  return new Response(null, { status: 204 })
}
