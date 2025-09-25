<script lang="ts">
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import type { PostView } from "$lib/model/types"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import CommunityName from "$lib/components/CommunityName.svelte"
  import { m } from "$lib/paraglide/messages.js"

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
    <!-- eslint-disable-next-line svelte/no-at-html-tags -->
    <div class="tiptap my-4">{@html post.body}</div>
  </div>
  <div class="divider"></div>
  <div class="text-sm">{m.comments_count({ count: 0 })}</div>
  <form class="mt-4">
    <fieldset class="fieldset">
      <legend class="fieldset-legend">{m.comment_add_legend()}</legend>
      <textarea class="textarea h-24" placeholder={m.comment_placeholder()}></textarea>
    </fieldset>
    <button type="submit" class="btn mt-4 btn-sm btn-primary" disabled={submitting}>
      {#if submitting}
        <span class="loading loading-spinner"></span>
      {:else}
        {m.comment_submit()}
      {/if}
    </button>
  </form>
</div>
