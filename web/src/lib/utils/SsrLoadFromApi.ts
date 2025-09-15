import { env } from "$env/dynamic/private"
import type { ServerLoadEvent } from "@sveltejs/kit"
import { Cookies } from "$lib/cookies"

export class SsrLoadFromApi {
  static async loadAuthLenient<T>(event: ServerLoadEvent, path: string): Promise<T | null> {
    const accessToken = this.getValidAccessToken(event)
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

  private static getValidAccessToken(event: ServerLoadEvent): string | undefined {
    const accessToken = event.cookies.get(Cookies.accessToken)
    if (accessToken) {
      const expiry: number = JSON.parse(atob(accessToken.split(".")[1])).exp
      if (Date.now() >= expiry * 1000) {
        return undefined
      }
      return accessToken
    }
    return undefined
  }
}
