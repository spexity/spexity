<script lang="ts">
  import "$lib/components/Editor.css"
  import { page, updated } from "$app/state"
  import { onMount } from "svelte"
  import { goto } from "$app/navigation"
  import { authManager } from "$lib/auth"
  import { AuthUserAccountState } from "$lib/utils/AuthManager.svelte"

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
</script>

<div class="app-body">
  <div class="w-full px-4">
    {@render children?.()}
  </div>
  <div class="navbar bg-base-100 shadow-sm">
    <div class="flex h-full flex-1 flex-col justify-center">
      <a href="/">
        <img width="40px" height="40px" src="/logo.png" alt="Spexity logo" />
      </a>
    </div>
    <div class="flex-none">
      <ul class="menu menu-horizontal px-1">
        <li><a href="/" class={active === "home" ? "menu-active" : ""}>Home</a></li>
        <li>
          <a href="/communities" class={active === "communities" ? "menu-active" : ""}
            >Communities</a
          >
        </li>
      </ul>
      <div class="dropdown dropdown-end">
        <div
          tabindex="0"
          role="button"
          aria-label="Account menu button"
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
          aria-label="Account menu content"
          class="dropdown-content menu mt-3 w-50 rounded-box bg-base-100 shadow"
        >
          {#if authManager.userAccount}
            <li>
              <a aria-label="Account profile" href="/account"
                >{authManager.userAccount.contributorHandle}</a
              >
            </li>
            <div class="divider m-0"></div>
            <li>
              <a href="/account">Account</a>
            </li>
            <li><a href="/#" onclick={signOut}>Sign Out</a></li>
          {:else}
            <li><a href="/#" onclick={signIn}>Sign In</a></li>
          {/if}
        </ul>
      </div>
    </div>
  </div>
  {#if updated.current}
    <div class="toast">
      <div class="alert alert-info">
        <span>A new version is available</span>
        <button class="btn btn-outline" onclick={() => location.reload()}>Reload</button>
      </div>
    </div>
  {/if}
</div>
