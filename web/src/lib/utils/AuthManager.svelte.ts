import { browser } from "$app/environment"
import { env } from "$env/dynamic/public"
import type { User } from "oidc-client-ts"
import { UserManager, WebStorageStateStore } from "oidc-client-ts"
import { type CurrentUserAccount, CurrentUserStorage } from "$lib/utils/CurrentUserStorage"
import { HttpClient } from "$lib/utils/HttpClient"
import { goto } from "$app/navigation"
import { authManager } from "$lib/auth"
import { Cookies } from "$lib/cookies"
import { resolve } from "$app/paths"

export enum AuthUserAccountState {
  INIT,
  NOT_LOGGED_IN,
  NOT_REGISTERED,
  LOGGED_IN,
  LOGGED_IN_VERIFIED,
}

interface SignInState {
  locationUri?: string
}

export class AuthManager {
  private readonly currentUserStorage?: CurrentUserStorage
  private readonly authManager?: UserManager
  private ignoreOidcUserLoaded: boolean = false
  userAccountState: AuthUserAccountState = $state(AuthUserAccountState.INIT)
  userAccount: CurrentUserAccount | null = $state(null)
  oidcUser: User | null = $state(null)
  httpClient = new HttpClient({
    base: env.PUBLIC_API_URL,
    authHeaderProvider: browser
      ? async () => {
          if (this.oidcUser) {
            return `Bearer ${this.oidcUser.access_token}`
          }
          return ""
        }
      : undefined,
  })

  constructor() {
    if (browser) {
      this.currentUserStorage = new CurrentUserStorage()
      this.authManager = new UserManager({
        authority: env.PUBLIC_OIDC_AUTH_SERVER_URL,
        client_id: env.PUBLIC_OIDC_CLIENT_ID,
        redirect_uri: `${window.location.protocol}//${window.location.host}/auth/login-redirect`,
        post_logout_redirect_uri: `${window.location.protocol}//${window.location.host}`,
        response_type: "code",
        scope: "openid email roles offline_access",
        response_mode: "fragment",
        filterProtocolClaims: true,
        automaticSilentRenew: true,
        revokeTokensOnSignout: true,
        userStore: new WebStorageStateStore({ store: window.localStorage }),
      })
      this.authManager.events.addUserLoaded((user) => {
        return this.handleOidcUserLoaded(user)
      })
      this.authManager.events.addAccessTokenExpired(() => {
        return this.handleOidcAccessTokenExpired()
      })
      this.authManager.events.addUserUnloaded(() => {
        return this.handleOidcUserUnloaded()
      })
      this.init()
    }
  }

  async signIn() {
    if (!this.authManager) {
      return Promise.reject("Cannot sign out")
    }
    return this.authManager.signinRedirect({
      state: {
        locationUri: window.location.pathname + window.location.search + window.location.hash,
      } as SignInState,
    })
  }

  async signInCallback() {
    try {
      const oidcUser = await this.authManager?.signinCallback()
      if (oidcUser) {
        await this.getRemoteCurrentUserAccount(oidcUser, true)
      } else {
        this.clearCurrentUserAccount()
        await goto(resolve("/"))
      }
    } catch (err) {
      console.error("Failed to correlate signing with callback", err)
      await goto(resolve("/"))
    }
  }

  async signOut() {
    if (!this.authManager) {
      return Promise.resolve()
    }
    this.ignoreOidcUserLoaded = true
    await this.authManager.signoutRedirect()
    this.ignoreOidcUserLoaded = false
  }

  async registerUserAccount(alias: string, acceptTermsAndConditions: boolean) {
    const registerResponse = await authManager.httpClient.post<CurrentUserAccount>(
      "/api/current-user",
      { alias, acceptTermsAndConditions },
    )
    this.setUserAccountLoggedIn(registerResponse)
    this.currentUserStorage?.set(registerResponse)
  }

  private async init() {
    if (!this.authManager || !this.currentUserStorage) {
      return
    }
    this.oidcUser = await this.authManager.getUser()
    if (this.oidcUser) {
      const userAccount = this.currentUserStorage.get()
      if (userAccount) {
        if (userAccount.authCorrelationId === this.oidcUser.profile.sub) {
          this.setUserAccountLoggedIn(userAccount)
        } else {
          this.currentUserStorage.clear()
        }
      }
      if (this.oidcUser.expired) {
        console.log("Loaded user expired, trying to reload")
        this.oidcUser = await this.authManager.signinSilent()
        if (!this.oidcUser) {
          console.log("Failed to reload user")
          this.setUserAccountNotLoggedIn()
        }
      }
      if (this.oidcUser) {
        await this.getRemoteCurrentUserAccount(this.oidcUser, false)
      }
    } else {
      this.clearCurrentUserAccount()
    }
  }

  private async getRemoteCurrentUserAccount(oidcUser: User, followState: boolean) {
    const currentUserResponse = await fetch(`${env.PUBLIC_API_URL}/api/current-user`, {
      headers: { authorization: `Bearer ${oidcUser.access_token}` },
    })
    if (currentUserResponse.status === 200) {
      const userAccount = await currentUserResponse.json()
      this.setUserAccountLoggedIn(userAccount)
      this.currentUserStorage?.set(userAccount)
      if (followState) {
        const state = oidcUser.state as SignInState | undefined
        if (state?.locationUri) {
          // @ts-expect-error loaded from localstorage
          await goto(resolve(state.locationUri))
        } else {
          await goto(resolve("/"))
        }
      }
    } else if (currentUserResponse.status === 404) {
      this.setUserAccountNotRegistered()
      await goto(resolve("/auth/register"))
    } else if (currentUserResponse.status === 401) {
      this.clearCurrentUserAccount()
      this.authManager?.signoutSilent()
    } else {
      console.error("Failed to assess current user status.")
      //TODO: what to do on unknown error?
    }
  }

  private clearCurrentUserAccount() {
    this.setUserAccountNotLoggedIn()
    this.clearDocumentAuthCookie()
    this.currentUserStorage?.clear()
  }

  private async handleOidcUserLoaded(oidcUser: User) {
    if (this.ignoreOidcUserLoaded) {
      return
    }
    //called by oidc-ts everytime the user access token is loaded
    console.log("user loaded")
    this.oidcUser = oidcUser
    this.setDocumentAuthCookie(oidcUser)
  }

  private async handleOidcUserUnloaded() {
    console.log("user unloaded")
    this.clearCurrentUserAccount()
  }

  private async handleOidcAccessTokenExpired() {
    console.log("token expired")
    try {
      await this.authManager?.signinSilent()
    } catch (err) {
      console.error("Could not silently login user", err)
      this.clearCurrentUserAccount()
    }
  }

  private setDocumentAuthCookie(user: User) {
    const now = Math.floor(Date.now() / 1000)
    const expiresAt = user.expires_at ?? now + 15 * 60
    const maxAge = Math.max(0, Math.floor(expiresAt - now))
    document.cookie =
      `${Cookies.accessToken}=${encodeURIComponent(user.access_token)}; ` +
      `path=/; ` +
      `max-age=${maxAge}; ` +
      `SameSite=Strict; ` +
      `Secure`
  }

  private clearDocumentAuthCookie() {
    document.cookie = `${Cookies.accessToken}=; path=/; max-age=0; SameSite=Strict; Secure`
  }

  //Auth State Manipulation
  private setUserAccountNotLoggedIn() {
    this.oidcUser = null
    this.userAccount = null
    this.userAccountState = AuthUserAccountState.NOT_LOGGED_IN
    document.cookie = `${Cookies.contributorId}=; path=/; max-age=0`
  }

  private setUserAccountLoggedIn(userAccount: CurrentUserAccount) {
    this.userAccount = userAccount
    if (userAccount.verifiedHuman) {
      this.userAccountState = AuthUserAccountState.LOGGED_IN_VERIFIED
    } else {
      this.userAccountState = AuthUserAccountState.LOGGED_IN
    }
    document.cookie = `${Cookies.contributorId}=${userAccount.contributor.id}; path=/; max-age=315360000`
  }

  private setUserAccountNotRegistered() {
    this.userAccount = null
    this.userAccountState = AuthUserAccountState.NOT_REGISTERED
    document.cookie = `${Cookies.contributorId}=; path=/; max-age=0`
  }
}
