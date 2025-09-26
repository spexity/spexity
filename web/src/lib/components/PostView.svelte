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

  let commenting = $state(false)
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
  <div class="flex items-center justify-between">
    <div class="text-sm">{m.comments_count({ count: 0 })}</div>
    {#if commenting}
      <button
        class="btn btn-sm"
        onclick={() => {
          commenting = false
        }}>{m.comment_discard()}</button
      >
    {:else}
      <button
        class="btn btn-sm btn-primary"
        onclick={() => {
          commenting = true
        }}>{m.comment_button()}</button
      >
    {/if}
  </div>
  {#if commenting}
    <form>
      <fieldset class="fieldset">
        <textarea class="textarea h-24" placeholder={m.drafting_placeholder()}></textarea>
      </fieldset>
      <button type="submit" class="btn mt-4 btn-sm btn-primary" disabled={submitting}>
        {#if submitting}
          <span class="loading loading-spinner"></span>
        {:else}
          {m.comment_submit()}
        {/if}
      </button>
    </form>
  {/if}
</div>
