<script lang="ts">
  import type { CommunityPreview } from "$lib/model/types"

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
      <span>0 Posts</span>
    </div>
    <a class="btn btn-sm" href="/communities/{community.id}">View</a>
  </div>
</div>
