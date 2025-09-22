<script lang="ts">
  import { authManager } from "$lib/auth"
  import CommunityForm from "$lib/components/CommunityForm.svelte"
  import GatedFeature from "$lib/components/GatedFeature.svelte"
</script>

{#if authManager.userAccount?.verifiedHuman}
  <div class="text-2xl">New Community</div>
  <CommunityForm />
{:else}
  <GatedFeature />
{/if}
