<script lang="ts">
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"

  let { data }: PageProps = $props()
</script>

{#each data.posts as post (post.id)}
  <PostPreview {post} community={post.community} timezone={data.timezone} />
  <div class="divider m-0"></div>
{/each}
