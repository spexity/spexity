<script lang="ts">
  import type { PageProps } from "./$types"
  import * as Card from "$lib/components/ui/card/index.js"

  let { data }: PageProps = $props()
</script>

{#each data.posts as post (post.id)}
  <Card.Root>
    <Card.Header>
      <Card.Title>{post.subject}</Card.Title>
      <Card.Description>Card Description</Card.Description>
    </Card.Header>
    <Card.Content>
      <p>{post.body}</p>
    </Card.Content>
    <Card.Footer>
      <a href="/p/{post.id}">10 comments</a>
    </Card.Footer>
  </Card.Root>
{/each}
