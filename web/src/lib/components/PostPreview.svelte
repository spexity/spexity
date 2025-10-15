<script lang="ts">
  import type { CommunityRef } from "$lib/model/types"
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import CommunityName from "$lib/components/CommunityName.svelte"
  import type { CommunityPreviewPost } from "$lib/model/types"
  import { m } from "$lib/paraglide/messages.js"
  import { resolve } from "$app/paths"
  import { prefs } from "$lib/state"

  interface PostPreviewProps {
    post: CommunityPreviewPost
    community?: CommunityRef
  }

  const { post, community }: PostPreviewProps = $props()
  const formattedDateTime = DateFormatter.formatUtcIsoAbsolute(
    post.createdAt,
    prefs.timezone,
    prefs.locale,
  )
</script>

<div class="spx-card">
  <div class="spx-card-header">
    <span class="text-xs">
      {#if community}<CommunityName {community} />{/if}
    </span>
    <div class="flex flex-wrap items-center gap-1 text-xs">
      <ContributorHandle contributor={post.contributor} testIdQualifier={post.id} />
      <span class="spx-text-subtle">•</span>
      <span class="spx-text-subtle">{formattedDateTime}</span>
    </div>
  </div>
  <a href={resolve(`/posts/${post.id}`)}>
    <h2 class="font-medium" data-testid="post-subject">{post.subject}</h2>
    <p class="spx-text-subtle text-sm">{post.bodyText}</p>
  </a>
  <div class="spx-card-footer">
    <span class="spx-text-subtle text-xs" data-testid={`post-preview-comments-count-${post.id}`}>
      {m.post_comments_count({ count: post.commentsCount })}
    </span>
    <a class="btn btn-sm" href={resolve(`/posts/${post.id}`)}>{m.view_action()}</a>
  </div>
</div>
