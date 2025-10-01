<script lang="ts">
  import type { CommunityPreview } from "$lib/model/types"
  import { m } from "$lib/paraglide/messages.js"
  import { resolve } from "$app/paths"

  interface CommunityPreviewProps {
    community: CommunityPreview
  }

  const { community }: CommunityPreviewProps = $props()
</script>

<div class="spx-card">
  <span class="font-medium">
    {community.name}
  </span>
  <div class="spx-card-footer">
    <span class="spx-text-subtle text-xs"
      >{m.community_posts_count({ count: community.postsCount })}</span
    >
    <a class="btn btn-sm" href={resolve(`/communities/${community.id}`)}>{m.button_view()}</a>
  </div>
</div>
