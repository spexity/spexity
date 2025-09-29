<script lang="ts">
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"

  let { data }: PageProps = $props()
</script>

<div class="mt-2 flex flex-col gap-2" data-testid="posts-list">
  {#each data.posts as post (post.id)}
    <PostPreview {post} community={post.community} timezone={data.timezone} />
  {/each}
</div>
