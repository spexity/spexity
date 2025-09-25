<script lang="ts">
  import "$lib/components/Editor.css"
  import { page, updated } from "$app/state"
  import { onMount } from "svelte"
  import { goto } from "$app/navigation"
  import { authManager } from "$lib/auth"
  import { AuthUserAccountState } from "$lib/utils/AuthManager.svelte"
  import { m } from "$lib/paraglide/messages.js"
  import { setLocale, getLocale, locales } from "$lib/paraglide/runtime"

  type MenuItem = "home" | "communities"

  const { children } = $props()

  const determineActiveMenuItem = (): MenuItem => {
    if (page.url.pathname.startsWith("/communities") || page.url.pathname.startsWith("/posts")) {
      return "communities"
    }
    return "home"
  }

  const active = $derived<MenuItem>(determineActiveMenuItem())

  onMount(async () => {
    if (authManager.userAccountState === AuthUserAccountState.NOT_REGISTERED) {
      await goto("/auth/register")
    }
  })

  const signIn = async (event: MouseEvent) => {
    event.preventDefault()
    await authManager.signIn()
  }

  const signOut = async (event: MouseEvent) => {
    event.preventDefault()
    await authManager.signOut()
  }

  const switchLanguage = (event: MouseEvent, locale: string) => {
    event.preventDefault()
    setLocale(locale, { reload: false })
    // Announce language change to screen readers
    const announcement = m.i18n_languagechanged1({ language: m[`i18n_locales_${locale.replace('-', '_')}`]() })
    announceToScreenReader(announcement)
  }

  const announceToScreenReader = (message: string) => {
    const liveRegion = document.getElementById('language-live-region')
    if (liveRegion) {
      liveRegion.textContent = message
    }
  }

  $: currentLocale = getLocale()
</script>

<div class="app-body">
  <div class="w-full px-4">
    {@render children?.()}
  </div>
  <div class="navbar bg-base-100 shadow-sm">
    <div class="flex h-full flex-1 flex-col justify-center">
      <a href="/">
        <img width="40px" height="40px" src="/logo.png" alt={m.brand_logo_alt()} />
      </a>
    </div>
    <div class="flex-none">
      <ul class="menu menu-horizontal px-1">
        <li><a href="/" class={active === "home" ? "menu-active" : ""}>{m.nav_home()}</a></li>
        <li>
          <a href="/communities" class={active === "communities" ? "menu-active" : ""}
            >{m.nav_communities()}</a
          >
        </li>
      </ul>
      <div class="dropdown dropdown-end">
        <div
          tabindex="0"
          role="button"
          aria-label={m.nav_account_button_aria()}
          class="btn avatar btn-circle {[
            AuthUserAccountState.LOGGED_IN,
            AuthUserAccountState.LOGGED_IN_VERIFIED,
          ].includes(authManager.userAccountState)
            ? 'btn-outline btn-primary'
            : 'btn-ghost'}"
        >
          {#if authManager.userAccountState === AuthUserAccountState.INIT}
            💭
          {:else if authManager.userAccountState === AuthUserAccountState.LOGGED_IN}
            🍯
          {:else if authManager.userAccountState === AuthUserAccountState.LOGGED_IN_VERIFIED}
            ✨
          {:else}
            👀
          {/if}
        </div>
        <ul
          aria-label={m.nav_account_menu_aria()}
          class="dropdown-content menu mt-3 w-50 rounded-box bg-base-100 shadow"
        >
          {#if authManager.userAccount}
            <li>
              <a aria-label={m.nav_account_profile_aria()} href="/account"
                >{authManager.userAccount.contributorHandle}</a
              >
            </li>
            <div class="divider m-0"></div>
            <li>
              <a href="/account">{m.nav_account_title()}</a>
            </li>
            <li>
              <details class="dropdown dropdown-nested">
                <summary class="flex cursor-pointer items-center" role="menuitem" tabindex="0">
                  <span>{m.i18n_language()}</span>
                </summary>
                <ul class="dropdown-content menu w-40 rounded-box bg-base-100 shadow-lg">
                  {#each locales as locale}
                    <li>
                      <button
                        class="btn btn-ghost btn-sm justify-start {currentLocale === locale ? 'btn-active' : ''}"
                        onclick={(e) => switchLanguage(e, locale)}
                        type="button"
                      >
                        {m[`i18n_locales_${locale.replace('-', '_')}`]()}
                      </button>
                    </li>
                  {/each}
                </ul>
              </details>
            </li>
            <li><a href="/#" onclick={signOut}>{m.auth_signout1()}</a></li>
          {:else}
            <li><a href="/#" onclick={signIn}>{m.auth_signin1()}</a></li>
          {/if}
        </ul>
      </div>
    </div>
  </div>
  {#if updated.current}
    <div class="toast">
      <div class="alert alert-info">
        <span>{m.app_update_available()}</span>
        <button class="btn btn-outline" onclick={() => location.reload()}>{m.app_update_reload()}</button>
      </div>
    </div>
  {/if}

  <!-- ARIA live region for language change announcements -->
  <div id="language-live-region" aria-live="polite" aria-atomic="true" class="sr-only"></div>
</div>
