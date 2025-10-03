import { AuthManager } from "$lib/utils/AuthManager.svelte"
import { PrefsManager } from "$lib/utils/PrefsManager.svelte"

export const auth = new AuthManager()
export const prefs = new PrefsManager()
