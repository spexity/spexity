<script lang="ts">
  import { updated } from "$app/state"
  import { onMount } from "svelte"
  import { goto } from "$app/navigation"
  import favicon from "$lib/assets/favicon.svg"
  import { authManager } from "$lib/auth"
  import { AuthUserAccountState } from "$lib/utils/AuthManager.svelte"

  const { children } = $props()

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

<svelte:head>
  <link rel="icon" href={favicon} />
</svelte:head>
<div class="app-body">
  <div class="navbar bg-base-100 shadow-sm">
    <div class="h-full flex-1">
      <a href="/">
        <img src={favicon} alt="Spexity logo" class="h-full" />
      </a>
    </div>
    <div class="flex-none">
      <ul class="menu menu-horizontal px-1">
        <li><a href="/" class="menu-active">Home</a></li>
        <li><a href="/communities">Communities</a></li>
        <li><a href="/topics">Topics</a></li>
      </ul>
      <div class="dropdown dropdown-end">
        <div
          tabindex="0"
          role="button"
          class="btn avatar btn-circle {authManager.userAccountState ===
          AuthUserAccountState.LOGGED_IN
            ? 'btn-outline btn-primary'
            : 'btn-ghost'}"
        >
          {#if authManager.userAccountState === AuthUserAccountState.INIT}
            <span class="loading loading-spinner"></span>
          {:else if authManager.userAccountState === AuthUserAccountState.LOGGED_IN}
            ✍️
          {:else}
            👀
          {/if}
        </div>
        <ul class="dropdown-content menu mt-3 w-50 rounded-box bg-base-100 shadow">
          {#if authManager.userAccount}
            <li>
              <a href="/account">{authManager.userAccount.contributorHandle}</a>
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
  {@render children?.()}
  {#if updated.current}
    <div class="toast">
      <div class="alert alert-info">
        <span>A new version is available</span>
        <button class="btn btn-outline" onclick={() => location.reload()}>Reload</button>
      </div>
    </div>
  {/if}
</div>
