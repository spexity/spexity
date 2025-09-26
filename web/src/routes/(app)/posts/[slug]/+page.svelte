<script lang="ts">
  import type { PageProps } from "./$types"
  import PostView from "$lib/components/PostView.svelte"

  let { data }: PageProps = $props()
</script>

<PostView post={data.post} comments={data.comments} timezone={data.timezone} />
