<script lang="ts">
  import type { PageProps } from "./$types"
  import PostView from "$lib/components/PostView.svelte"

  let { data }: PageProps = $props()
</script>

<svelte:head>
  <title>{data.post.subject} | Spexity</title>
</svelte:head>

<PostView
  post={data.post}
  comments={data.comments}
  currentContributorId={data.currentContributorId}
/>
