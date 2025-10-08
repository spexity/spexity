<script lang="ts">
  import "$lib/components/Editor.css"
  import { page, updated } from "$app/state"
  import { onMount } from "svelte"
  import { goto } from "$app/navigation"
  import { auth, prefs } from "$lib/state"
  import { AuthUserAccountState } from "$lib/utils/AuthManager.svelte"
  import { m } from "$lib/paraglide/messages.js"
  import { type Locale, locales, setLocale } from "$lib/paraglide/runtime"
  import { LOCALES_MAP } from "$lib/locales"
  import { resolve } from "$app/paths"
  import { type Theme } from "$lib/utils/ThemeHandler"

  type MenuItem = "home" | "discover" | "communities"

  const { children, data } = $props()
  let langModalRef = $state<HTMLDialogElement>()

  const determineActiveMenuItem = (): MenuItem => {
    if (page.url.pathname.startsWith("/communities") || page.url.pathname.startsWith("/posts")) {
      return "communities"
    }
    if (page.url.pathname.startsWith("/discover")) {
      return "discover"
    }
    return "home"
  }

  let active = $derived<MenuItem>(determineActiveMenuItem())
  let loggedIn = $derived(
    [AuthUserAccountState.LOGGED_IN, AuthUserAccountState.LOGGED_IN_VERIFIED].includes(
      auth.userAccountState,
    ),
  )

  onMount(async () => {
    if (auth.userAccountState === AuthUserAccountState.NOT_REGISTERED) {
      await goto(resolve("/auth/register"))
    }
  })

  const signIn = async (event: MouseEvent) => {
    event.preventDefault()
    await auth.signIn()
  }

  const signOut = async (event: MouseEvent) => {
    event.preventDefault()
    await auth.signOut()
  }

  const showChangeLanguageModal = (event: MouseEvent) => {
    event.preventDefault()
    langModalRef?.showModal()
  }

  const switchLanguage = (event: MouseEvent, locale: Locale) => {
    event.preventDefault()
    setLocale(locale)
  }

  const switchTheme = (event: MouseEvent, theme: Theme) => {
    event.preventDefault()
    prefs.setTheme(theme)
  }
</script>

<dialog bind:this={langModalRef} class="modal">
  <div class="spx-modal-box modal-box">
    <form method="dialog">
      <button class="spx-modal-close-btn">✕</button>
    </form>
    <div class="flex flex-col gap-2 py-6">
      {#each locales as locale (locale)}
        <button
          class="btn btn-ghost btn-sm {data.prefs.locale === locale ? 'btn-active' : ''}"
          onclick={(e) => switchLanguage(e, locale)}
          type="button"
          data-testid={"locale-btn-" + locale}
        >
          {LOCALES_MAP[locale]?.label}
        </button>
      {/each}
    </div>
  </div>
  <form method="dialog" class="modal-backdrop">
    <button>close</button>
  </form>
</dialog>
<div class="spx-app">
  <div class="spx-navbar">
    <div class="flex items-center gap-2">
      <a href={resolve("/")}>
        <img
          width="40"
          height="40"
          src="/logo.png"
          alt={m.brand_logo_alt()}
          data-testid="brand-logo"
        />
      </a>
      <nav>
        <ul class="tabs-border tabs">
          <li>
            <a href={resolve("/")} class={["tab", active === "home" && "tab-active"]}
              >{m.nav_home()}</a
            >
          </li>
          {#if auth.ssrAwareContributorId}
            <li>
              <a href={resolve("/discover")} class={["tab", active === "discover" && "tab-active"]}
                >{m.nav_discover()}</a
              >
            </li>
          {/if}
          <li>
            <a
              href={resolve("/communities")}
              class={["tab", active === "communities" && "tab-active"]}>{m.nav_communities()}</a
            >
          </li>
        </ul>
      </nav>
    </div>
    <div class="flex-1"></div>
    <div class="flex">
      <div class="dropdown dropdown-end">
        <div
          tabindex="0"
          role="button"
          aria-label={m.nav_account_button_aria()}
          data-testid="account-menu-button"
          style={loggedIn
            ? `background-color: ${auth.userAccount?.contributor.avatarBgColor}`
            : null}
          class={[loggedIn ? "btn rounded-full p-2" : "btn btn-circle btn-ghost btn-soft"]}
        >
          {#if auth.userAccountState === AuthUserAccountState.INIT}
            💭
          {:else if loggedIn}
            {auth.userAccount?.contributor.avatarText}
          {:else}
            👀
          {/if}
        </div>
        <ul
          aria-label={m.nav_account_menu_aria()}
          data-testid="account-menu-content"
          class="dropdown-content menu mt-3 w-50 rounded-box bg-base-100 shadow"
        >
          {#if auth.userAccount}
            <li>
              <a aria-label={m.nav_account_profile_aria()} href={resolve("/account")}
                >{auth.userAccount.contributor.handle}</a
              >
            </li>
            <div class="divider m-0"></div>
            <li>
              <a href={resolve("/account")}>{m.nav_account_title()}</a>
            </li>
          {/if}
          <li>
            <details>
              <summary>{m.theme()}</summary>
              <ul>
                <li>
                  <a href={resolve("/")} onclick={(e) => switchTheme(e, "light")}>
                    {m.theme_light()}{prefs.theme === "light" ? " ✓" : ""}
                  </a>
                </li>
                <li>
                  <a href={resolve("/")} onclick={(e) => switchTheme(e, "system")}>
                    {m.theme_system()}{prefs.theme === "system" ? " ✓" : ""}
                  </a>
                </li>
                <li>
                  <a href={resolve("/")} onclick={(e) => switchTheme(e, "dark")}>
                    {m.theme_dark()}{prefs.theme === "dark" ? " ✓" : ""}
                  </a>
                </li>
              </ul>
            </details>
          </li>
          <li>
            <a
              href={resolve("/")}
              data-testid="account-language-link"
              onclick={showChangeLanguageModal}
            >
              {m.i18n_language()} 🌐
            </a>
          </li>
          <div class="divider m-0"></div>
          {#if auth.userAccount}
            <li>
              <a href={resolve("/")} data-testid="sign-out-link" onclick={signOut}
                >{m.auth_signOut()}</a
              >
            </li>
          {:else}
            <li>
              <a href={resolve("/")} data-testid="sign-in-link" onclick={signIn}
                >{m.auth_signIn()}</a
              >
            </li>
          {/if}
        </ul>
      </div>
    </div>
  </div>
  <div class="w-full px-4">
    {@render children?.()}
  </div>
  {#if updated.current}
    <div class="toast">
      <div class="alert alert-info">
        <span>{m.app_update_available()}</span>
        <button class="btn btn-outline" onclick={() => location.reload()}
          >{m.app_update_reload()}</button
        >
      </div>
    </div>
  {/if}
  <!--Init testing helper-->
  {#if auth.userAccountState === AuthUserAccountState.INIT}
    <div data-testid="init-in-progress"></div>
  {/if}
</div>
