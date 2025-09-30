<script lang="ts">
  import { resolve } from "$app/paths"
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"
  import { m } from "$lib/paraglide/messages.js"

  const { data }: PageProps = $props()
  const community = data.community
</script>

<div class="mb-4 flex flex-row justify-between">
  <div class="text-2xl">
    {community.name}
  </div>
  <a
    class="btn btn-sm"
    href={resolve(`/posts/new?communityId=${community.id}`)}
    data-testid="create-post-button">{m.post_create_action()}</a
  >
</div>
<div class="mt-2 flex flex-col gap-2" data-testid="posts-list">
  {#each data.posts as post (post.id)}
    <PostPreview {post} timezone={data.timezone} />
  {/each}
</div>
