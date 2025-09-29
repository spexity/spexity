<script lang="ts">
  import type { CommunityRef } from "$lib/model/types"
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import CommunityName from "$lib/components/CommunityName.svelte"
  import type { CommunityPreviewPost } from "$lib/model/types"
  import { m } from "$lib/paraglide/messages.js"
  import { resolve } from "$app/paths"

  interface PostPreviewProps {
    post: CommunityPreviewPost
    community?: CommunityRef
    timezone: string
  }

  const { post, community, timezone }: PostPreviewProps = $props()
  const formattedDateTime = DateFormatter.formatUtcIsoAbsolute(post.createdAt, timezone)
</script>

<div class="flex flex-col gap-1 rounded-lg border border-base-300 p-3">
  <div class="flex flex-row justify-between">
    <span class="text-xs">
      {#if community}<CommunityName {community} />{/if}
    </span>
    <div class="flex flex-wrap items-center gap-1 text-xs">
      <ContributorHandle contributor={post.contributor} testIdQualifier={post.id} />
      <span class="text-subtle">•</span>
      <span class="text-subtle">{formattedDateTime}</span>
    </div>
  </div>
  <a href={resolve(`/posts/${post.id}`)}>
    <h2 class="font-medium">{post.subject}</h2>
    <p class="text-subtle text-sm">{post.bodyText}</p>
  </a>
  <div class="flex flex-row justify-between">
    <div class="flex flex-row items-center gap-2 text-xs">
      <span class="text-subtle" data-testid={`post-preview-comments-count-${post.id}`}>
        {m.post_comments_count({ count: post.commentsCount })}
      </span>
    </div>
    <a class="btn btn-sm" href={resolve(`/posts/${post.id}`)}>{m.button_view()}</a>
  </div>
</div>
