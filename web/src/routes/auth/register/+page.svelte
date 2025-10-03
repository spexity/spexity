<script lang="ts">
  import { goto } from "$app/navigation"
  import { resolve } from "$app/paths"
  import { auth } from "$lib/state"
  import { AuthUserAccountState } from "$lib/utils/AuthManager.svelte"
  import { m } from "$lib/paraglide/messages.js"
  import { bgColors, randomEmoji, randomItem } from "$lib/utils/Utils"
  import AccountForm from "$lib/components/AccountForm.svelte"

  $effect(() => {
    if (
      auth.userAccountState !== AuthUserAccountState.INIT &&
      auth.userAccountState !== AuthUserAccountState.NOT_REGISTERED
    ) {
      goHome()
    }
  })

  const goHome = async () => {
    await goto(resolve("/"))
  }
</script>

<div class="flex h-screen flex-col items-center justify-center">
  {#if auth.userAccountState === AuthUserAccountState.INIT}
    <span class="loading loading-lg loading-spinner"></span>
    <p class="mt-4">{m.loading_redirecting()}</p>
  {:else}
    <AccountForm
      initAvatarText={randomEmoji() + randomEmoji()}
      initAvatarBgColor={randomItem(bgColors)}
      onsuccess={async () => {
        await goto(resolve("/"))
      }}
      mode="register"
    />
  {/if}
</div>
