const CURRENT_USER_KEY = "spexity.user"

export interface CurrentUserAccount {
  id: string
  verifiedHuman: boolean
  authCorrelationId: string
  contributorId: string
  contributorHandle: string
}

export class CurrentUserStorage {
  get(): CurrentUserAccount | null {
    const val = localStorage.getItem(CURRENT_USER_KEY)
    if (val != null && val.length > 0) {
      return JSON.parse(val)
    }
    return null
  }

  set(user: CurrentUserAccount) {
    localStorage.setItem(CURRENT_USER_KEY, JSON.stringify(user))
  }

  clear() {
    localStorage.removeItem(CURRENT_USER_KEY)
  }
}
