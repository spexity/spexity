<script lang="ts">
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"

  let { data }: PageProps = $props()
</script>

<div class="flex w-full flex-col px-4">
  {#each data.posts as post (post.id)}
    <PostPreview {post} />
    <div class="divider m-0"></div>
  {/each}
</div>