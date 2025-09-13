import { env } from "$env/dynamic/private"
import type { ServerLoadEvent } from "@sveltejs/kit"
import { Cookies } from "$lib/cookies"

export class SsrLoadFromApi {
  static async loadAuthLenient<T>(event: ServerLoadEvent, path: string): Promise<T | null> {
    const accessToken = event.cookies.get(Cookies.accessToken)
    const data = await this.load<T>(event, path, accessToken)
    if (data == null && accessToken) {
      return (await this.load(event, path)) as T
    }
    return data as T
  }

  private static async load<T>(event: ServerLoadEvent, path: string, accessToken?: string) {
    try {
      const response = await event.fetch(`${env.API_URL}${path}`, {
        headers: accessToken
          ? {
              Authorization: `Bearer ${accessToken}`,
            }
          : undefined,
      })
      if (response.ok) {
        return (await response.json()) as T
      }
    } catch (err) {
      console.log("Error fetching data", err)
    }
    return null
  }
}
