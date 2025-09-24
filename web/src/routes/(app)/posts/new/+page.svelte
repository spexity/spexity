<script lang="ts">
  import type { PageProps } from "./$types"
  import { authManager } from "$lib/auth"
  import PostForm from "$lib/components/PostForm.svelte"
  import GatedFeature from "$lib/components/GatedFeature.svelte"

  const { data }: PageProps = $props()
</script>

{#if authManager.userAccount?.verifiedHuman}
  <div class="text-2xl">
    New Post to {data.communityName}
  </div>
  <PostForm communityId={data.communityId} />
{:else}
  <GatedFeature />
{/if}
