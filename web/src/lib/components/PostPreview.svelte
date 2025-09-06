<script lang="ts">

  import type { PostPreview } from "$lib/components/PostPreview"
  import { PostDateFormatter } from "$lib/utils/PostDateFormatter"

  interface PostPreviewProps {
    post: PostPreview
  }

  const props: PostPreviewProps = $props()
  const post = props.post
  const formattedDateTime = PostDateFormatter.formatUtcIsoAbsolute(post.createdAt)

</script>

<div class="card-s card m-3 bg-base-200 shadow-sm">
  <div class="card-body">
      <span class="text-xs">
        {post.contributorHandle} to {post.communityName} - {formattedDateTime}
      </span>
    <h2 class="card-title">{post.subject}</h2>
    <p>{post.body}</p>
    <div class="card-actions justify-end">
      <a href="/p/{post.id}">View</a>
    </div>
  </div>
</div>