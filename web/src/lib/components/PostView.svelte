<script lang="ts">
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import type { PostView } from "$lib/model/types"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import CommunityName from "$lib/components/CommunityName.svelte"

  interface PostViewProps {
    post: PostView
    timezone: string
  }

  const { post, timezone }: PostViewProps = $props()
  const formattedDateTime = DateFormatter.formatUtcIsoAbsolute(post.createdAt, timezone)

  let submitting = $state<boolean>(false)
</script>

<div class="flex flex-col">
  <div class="flex flex-row justify-between">
    <span class="text-xs font-medium">
      <CommunityName community={post.community} />
    </span>
    <div class="text-xs">
      <ContributorHandle contributor={post.contributor} />
      - {formattedDateTime}
    </div>
  </div>
  <div class="mt-4">
    <h2 class="text-2xl font-medium">{post.subject}</h2>
    <div class="tiptap my-4">{@html post.body}</div>
  </div>
  <div class="divider"></div>
  <div class="text-sm">0 Comments</div>
  <form class="mt-4">
    <fieldset class="fieldset">
      <legend class="fieldset-legend">Add comment</legend>
      <textarea class="textarea h-24" placeholder="Start typing"></textarea>
    </fieldset>
    <button type="submit" class="btn mt-4 btn-sm btn-primary" disabled={submitting}>
      {#if submitting}
        <span class="loading loading-spinner"></span>
      {:else}
        Post Comment
      {/if}
    </button>
  </form>
</div>
