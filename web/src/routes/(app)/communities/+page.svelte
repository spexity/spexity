<script lang="ts">
  import { resolve } from "$app/paths"
  import type { PageProps } from "./$types"
  import CommunityPreview from "$lib/components/CommunityPreview.svelte"
  import { m } from "$lib/paraglide/messages.js"

  let { data }: PageProps = $props()
</script>

<div class="mb-4 flex flex-row justify-end">
  <a class="btn btn-sm" href={resolve("/communities/new")} data-testid="start-community-button"
    >{m.community_start()}</a
  >
</div>
<div class="mt-2 flex flex-col gap-2" data-testid="communities-list">
  {#each data.communities as community (community.id)}
    <CommunityPreview {community} />
  {/each}
</div>
