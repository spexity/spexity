<script lang="ts">
  import type { CommunityRef } from "$lib/model/types"
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import CommunityName from "$lib/components/CommunityName.svelte"
  import type { CommunityPreviewPost } from "$lib/model/types.js"

  interface PostPreviewProps {
    post: CommunityPreviewPost
    community?: CommunityRef
    timezone: string
  }

  const { post, community, timezone }: PostPreviewProps = $props()
  const formattedDateTime = DateFormatter.formatUtcIsoAbsolute(post.createdAt, timezone)
</script>

<div class="flex flex-col">
  <div class="flex flex-row justify-between">
    <span class="text-xs font-medium">
      {#if community}<CommunityName {community} />{/if}
    </span>
    <div class="text-xs">
      <ContributorHandle contributor={post.contributor} />
      - {formattedDateTime}
    </div>
  </div>
  <div>
    <h2 class="font-medium">{post.subject}</h2>
    <p class="text-sm">{post.body}</p>
  </div>
  <div class="flex flex-row justify-end">
    <a class="btn btn-sm" href="/posts/{post.id}">View</a>
  </div>
</div>
