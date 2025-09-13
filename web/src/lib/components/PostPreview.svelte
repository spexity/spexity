<script lang="ts">
  import type { PostPreview } from "$lib/components/PostPreview"
  import { PostDateFormatter } from "$lib/utils/PostDateFormatter"

  interface PostPreviewProps {
    post: PostPreview
    timezone: string
  }

  const props: PostPreviewProps = $props()
  const post = props.post
  const formattedDateTime = PostDateFormatter.formatUtcIsoAbsolute(post.createdAt, props.timezone)
  const contributorName = post.contributorHandle.substring(0, post.contributorHandle.indexOf("#"))
  const contributorDiscriminator = post.contributorHandle.substring(contributorName.length)
</script>

<div class="flex flex-col">
  <div class="flex flex-row justify-between">
    <span class="text-xs font-medium">
      {post.communityName}
    </span>
    <div class="text-xs">
      {contributorName}<span class="text-base-content/50">{contributorDiscriminator}</span> - {formattedDateTime}
    </div>
  </div>
  <div>
    <h2 class="font-medium">{post.subject}</h2>
    <p class="text-sm">{post.body}</p>
  </div>
  <div class="flex flex-row justify-end">
    <a class="btn btn-sm" href="/p/{post.id}">View</a>
  </div>
</div>
