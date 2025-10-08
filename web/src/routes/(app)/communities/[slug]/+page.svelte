<script lang="ts">
  import { resolve } from "$app/paths"
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"
  import { m } from "$lib/paraglide/messages.js"
  import { auth } from "$lib/state"
  import { goto } from "$app/navigation"

  const { data }: PageProps = $props()
  const community = data.community

  let isMember = $state<boolean>(community.member)
  let loading = $state<boolean>(false)

  const toggleMembership = async () => {
    if (!auth.userAccount) {
      await goto(resolve("/register"))
    }
    try {
      loading = true
      if (isMember) {
        await auth.httpClient.delete(`/api/contributors/current/communities/${community.id}`)
        isMember = false
      } else {
        await auth.httpClient.post(`/api/contributors/current/communities/${community.id}`, {})
        isMember = true
      }
    } catch (err) {
      console.error("Failed to toggle membership", err)
    } finally {
      loading = false
    }
  }
</script>

<div class="mb-4 flex flex-row justify-between">
  <div class="text-2xl">
    {community.name}
  </div>
  <div class="flex gap-2">
    {#if isMember}
      <a
        class="btn btn-sm"
        href={resolve(`/posts/new?communityId=${community.id}`)}
        data-testid="create-post-button">{m.post_create_action()}</a
      >
      <button
        class="btn btn-sm"
        onclick={toggleMembership}
        disabled={loading}
        data-testid="leave-community-button"
      >
        {#if loading}
          <span class="loading loading-spinner"></span>
        {:else}
          {m.community_leave()}
        {/if}
      </button>
    {:else}
      <button
        class="btn btn-sm"
        onclick={toggleMembership}
        disabled={loading}
        data-testid="join-community-button"
      >
        {#if loading}
          <span class="loading loading-spinner"></span>
        {:else}
          {m.community_join()}
        {/if}
      </button>
    {/if}
  </div>
</div>
<div class="spx-cards-list" data-testid="posts-list">
  {#each data.posts as post (post.id)}
    <PostPreview {post} />
  {/each}
</div>
