<script lang="ts">
  import type { CommunityPreview } from "$lib/model/types"
  import { m } from "$lib/paraglide/messages.js"
  import { resolve } from "$app/paths"

  interface CommunityPreviewProps {
    community: CommunityPreview
  }

  const { community }: CommunityPreviewProps = $props()
</script>

<div class="flex flex-col">
  <div class="flex flex-row justify-between">
    <span class="font-medium">
      {community.name}
    </span>
  </div>
  <div class="flex flex-row justify-between">
    <div class="flex flex-row items-center gap-2 text-xs">
      <span>{m.community_posts_count({ count: 0 })}</span>
    </div>
    <a class="btn btn-sm" href={resolve(`/communities/${community.id}`)}>{m.button_view()}</a>
  </div>
</div>
