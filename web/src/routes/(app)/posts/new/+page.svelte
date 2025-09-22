<script lang="ts">
  import type { PageProps } from "./$types"
  import { authManager } from "$lib/auth"
  import PostForm from "$lib/components/PostForm.svelte"
  import GatedFeature from "$lib/components/GatedFeature.svelte"

  const { data }: PageProps = $props()
  const community = data.community
</script>

{#if authManager.userAccount?.verifiedHuman}
  <div class="text-2xl">
    New Post to {community.name}
  </div>
  <PostForm communityId={community.id} />
{:else}
  <GatedFeature />
{/if}
