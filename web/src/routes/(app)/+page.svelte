<script lang="ts">
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"

  let { data }: PageProps = $props()
</script>

<div class="spx-cards-list" data-testid="posts-list">
  {#each data.posts as post (post.id)}
    <PostPreview {post} community={post.community} />
  {/each}
</div>
