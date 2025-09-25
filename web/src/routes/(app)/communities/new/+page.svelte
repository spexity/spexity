<script lang="ts">
  import { authManager } from "$lib/auth"
  import CommunityForm from "$lib/components/CommunityForm.svelte"
  import GatedFeature from "$lib/components/GatedFeature.svelte"
  import { m } from "$lib/paraglide/messages.js"
</script>

{#if authManager.userAccount?.verifiedHuman}
  <div class="text-2xl">{m.community_new_title()}</div>
  <CommunityForm />
{:else}
  <GatedFeature />
{/if}
