<script lang="ts">
  import { m } from "$lib/paraglide/messages.js"
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"

  let { data }: PageProps = $props()
</script>

<svelte:head>
  <title>{m.nav_discover()} | Spexity</title>
</svelte:head>

<div class="spx-cards-list" data-testid="posts-list">
  {#each data.posts as post (post.id)}
    <PostPreview {post} community={post.community} />
  {/each}
</div>
