<script lang="ts">
  import type { PageProps } from "./$types"

  let { data }: PageProps = $props()
</script>

{#each data.posts as post (post.id)}
  <div class="card-s card m-3 bg-base-200 shadow-sm">
    <div class="card-body">
      <span class="text-xs"
        >{post.contributorHandle} to {post.communityName} - {post.createdAt}</span
      >
      <h2 class="card-title">{post.subject}</h2>
      <p>{post.body}</p>
      <div class="card-actions justify-end">
        <a href="/p/{post.id}">View</a>
      </div>
    </div>
  </div>
{/each}
