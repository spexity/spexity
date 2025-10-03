<script lang="ts">
  import { m } from "$lib/paraglide/messages.js"
  import { auth } from "$lib/state"
  import AccountForm from "$lib/components/AccountForm.svelte"
</script>

<svelte:head>
  <title>{m.nav_account_title()} | Spexity</title>
</svelte:head>
{#if auth.userAccount}
  <div class="flex min-h-[50vh] flex-col items-center justify-center">
    <AccountForm
      initAlias={auth.userAccount.contributor.handle.split("#")[0]}
      initDiscriminator={auth.userAccount.contributor.handle.split("#")[1]}
      initAvatarText={auth.userAccount.contributor.avatarText}
      initAvatarBgColor={auth.userAccount.contributor.avatarBgColor}
      onsuccess={() => {}}
      mode="update"
    />
  </div>
{/if}
