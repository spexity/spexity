<script lang="ts">
  import type { PageProps } from "./$types"

  let { data }: PageProps = $props()
</script>

{#each data.posts as post (post.id)}
  <div>{post.subject}</div>
  <p>{post.body}</p>
  <a href="/p/{post.id}">10 comments</a>
{/each}
