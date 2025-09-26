<script lang="ts">
  import { resolve } from "$app/paths"
  import type { PageProps } from "./$types"
  import CommunityPreview from "$lib/components/CommunityPreview.svelte"
  import { m } from "$lib/paraglide/messages.js"

  let { data }: PageProps = $props()
</script>

<div class="mb-4 flex flex-row justify-end">
  <a class="btn btn-sm" href={resolve("/communities/new")}>{m.community_start()}</a>
</div>
{#each data.communities as community (community.id)}
  <CommunityPreview {community} />
  <div class="divider m-0"></div>
{/each}
