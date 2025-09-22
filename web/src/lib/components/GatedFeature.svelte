<script lang="ts">
  import { authManager } from "$lib/auth"
</script>

<div class="flex min-h-[50vh] flex-col items-center justify-center text-center">
  <div class="flex flex-row justify-center">
    <span class="text-2xl font-semibold">
      {#if authManager.userAccount}
        You need to be verified as a human before proceeding.
      {:else}
        You need to sign in and verify humanity.
      {/if}
    </span>
  </div>
</div>
