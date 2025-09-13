<script lang="ts">
  import type { CommunityPreview } from "$lib/components/CommunityPreview"

  interface CommunityPreviewProps {
    community: CommunityPreview
  }

  const props: CommunityPreviewProps = $props()
  const community = props.community
</script>

<div class="flex flex-col">
  <div class="flex flex-row justify-between">
    <span class="font-medium">
      {community.name}
    </span>
  </div>
  <div class="flex flex-row justify-end">
    <a class="btn btn-sm" href="/communities/{community.id}">View</a>
  </div>
</div>
