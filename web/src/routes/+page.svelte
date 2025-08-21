<script lang="ts">
  import type { PageProps } from "./$types"

  let { data }: PageProps = $props()
</script>

<h1>Welcome to Spexity</h1>

{#each data.posts as post (post.id)}
  <a href="/p/{post.id}">{post.subject}</a>
  <span>{post.body}</span>
  <br />
{/each}
