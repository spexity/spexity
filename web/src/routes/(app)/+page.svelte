<script lang="ts">
  import { m } from "$lib/paraglide/messages.js"
  import type { PageProps } from "./$types"
  import PostPreview from "$lib/components/PostPreview.svelte"
  import { resolve } from "$app/paths"

  let { data }: PageProps = $props()
</script>

<svelte:head>
  <title>{m.nav_home()} | Spexity</title>
</svelte:head>

{#if data.posts.length === 0}
  <div class="spx-empty-message">
    <span>{m.home_timeline_empty_message()}</span>
    <a href={resolve("/discover")} class="btn btn-soft btn-sm btn-primary">{m.nav_discover()}</a>
  </div>
{/if}

<div class="spx-cards-list" data-testid="posts-list">
  {#each data.posts as post (post.id)}
    <PostPreview {post} community={post.community} />
  {/each}
</div>

<style>
  .spx-empty-message {
    text-align: center;
    font-size: 1.25rem;
    margin-top: 10rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 1rem;
  }
</style>
