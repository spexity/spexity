<script lang="ts">
  import type { PageProps } from "./$types"
  import CommunityPreview from "$lib/components/CommunityPreview.svelte"

  let { data }: PageProps = $props()
</script>

<div class="mb-4 flex flex-row justify-end">
  <a class="btn btn-sm" href="/communities/new">Start a Community</a>
</div>
{#each data.communities as community (community.id)}
  <CommunityPreview {community} />
  <div class="divider m-0"></div>
{/each}
