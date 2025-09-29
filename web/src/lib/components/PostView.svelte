<script lang="ts">
  import { authManager } from "$lib/auth"
  import CommunityName from "$lib/components/CommunityName.svelte"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import Editor from "$lib/components/Editor.svelte"
  import type { CommentPage, CommentView, PostView } from "$lib/model/types"
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import { EditorUtils } from "$lib/utils/EditorUtils"
  import { HttpError } from "$lib/utils/HttpClient"
  import { m } from "$lib/paraglide/messages.js"
  import PostCommentView from "$lib/components/PostCommentView.svelte"

  interface PostViewProps {
    post: PostView
    timezone: string
    comments: CommentPage
  }

  const { post, timezone, comments: initialComments }: PostViewProps = $props()
  const formattedDateTime = DateFormatter.formatUtcIsoAbsolute(post.createdAt, timezone)

  let comments = $state<CommentView[]>(initialComments.items)
  let commentsMeta = $state({
    page: initialComments.page,
    pageSize: initialComments.pageSize,
  })
  let commentsCount = $state(post.commentsCount)

  let commenting = $state(false)
  let submitting = $state(false)
  let commentError = $state<string | undefined>()
  let editorRef = $state<Editor>()

  let loadMoreBusy = $state(false)
  let loadMoreError = $state<string | undefined>()

  const hasMore = $derived(comments.length < commentsCount)

  const startCommenting = () => {
    commenting = true
    commentError = undefined
  }

  const cancelCommenting = () => {
    commenting = false
    commentError = undefined
  }

  const handleCreateComment = async (event: SubmitEvent) => {
    event.preventDefault()
    try {
      commentError = undefined
      const body = editorRef?.getValue()
      if (!EditorUtils.hasMeaningfulText(body)) {
        commentError = m.comment_error_empty()
        return
      }
      submitting = true
      const bodyHtml = editorRef?.getValueHtml() ?? ""
      const created = await authManager.httpClient.post<{ id: string }>(
        `/api/posts/${post.id}/comments`,
        { bodyDocument: body },
      )
      comments = [
        ...comments,
        {
          id: created.id,
          createdAt: new Date().toISOString(),
          editCount: 0,
          deleted: false,
          contributor: {
            id: authManager.userAccount?.contributorId ?? "",
            handle: authManager.userAccount?.contributorHandle ?? "",
          },
          bodyHtml,
        },
      ]
      commentsCount += 1
      editorRef?.setHtmlValue("")
      commenting = false
    } catch (err) {
      if (err instanceof HttpError && err.status === 429) {
        commentError = m.comment_error_throttled()
      } else {
        commentError = m.comment_error_failed()
      }
    } finally {
      submitting = false
    }
  }

  const loadMore = async () => {
    if (loadMoreBusy || !hasMore) {
      return
    }
    loadMoreBusy = true
    loadMoreError = undefined
    const nextPage = commentsMeta.page + 1
    try {
      const response = await authManager.httpClient.get<CommentPage>(
        `/api/posts/${post.id}/comments?page=${nextPage}&pageSize=${commentsMeta.pageSize}`,
      )
      const existingIds = new Set(comments.map((item) => item.id))
      const nextItems = response.items.filter((item) => !existingIds.has(item.id))
      comments = [...comments, ...nextItems]
      commentsMeta = {
        page: response.page,
        pageSize: response.pageSize,
      }
    } catch {
      loadMoreError = m.comment_error_failed()
    } finally {
      loadMoreBusy = false
    }
  }

  const handleLoadMoreKey = (event: KeyboardEvent) => {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault()
      loadMore()
    }
  }

  const onCommentDeleted = (id: string) => {
    comments = comments.map((current) => {
      let deletedComment: CommentView = {
        ...current,
        bodyHtml: null,
        deleted: true,
      }
      return current.id === id ? deletedComment : current
    })
    commentsCount = Math.max(0, commentsCount - 1)
  }

  const onCommentEdited = (comment: CommentView) => {
    comments = comments.map((current) => (current.id === comment.id ? comment : current))
  }
</script>

<div class="flex flex-col" data-testid="post-view">
  <div class="flex flex-row justify-between">
    <span class="text-lg font-medium">
      <CommunityName community={post.community} />
    </span>
    <div class="flex flex-wrap items-center gap-1 text-xs">
      <ContributorHandle contributor={post.contributor} testIdQualifier={post.id} />
      <span class="text-subtle">•</span>
      <span class="text-subtle">{formattedDateTime}</span>
    </div>
  </div>
  <div class="mt-4">
    <h2 class="text-2xl font-medium">{post.subject}</h2>
    <!-- eslint-disable-next-line svelte/no-at-html-tags -->
    <div class="tiptap my-4">{@html post.bodyHtml}</div>
  </div>
  <div class="divider m-0"></div>
  <div class="flex items-center justify-between">
    <div class="text-subtle text-sm" data-testid="comments-count">
      {m.comments_count({ count: commentsCount })}
    </div>
    <button
      class={["btn btn-sm", commenting && "invisible"]}
      data-testid="comment-toggle"
      onclick={startCommenting}>{m.comment_button()}</button
    >
  </div>
  {#if commenting}
    <form class="my-2 flex flex-col gap-2" onsubmit={handleCreateComment} autocomplete="off">
      <Editor bind:this={editorRef} id="new-comment" dataTestId="comment-editor" mode="comment" />
      {#if commentError}
        <div role="alert" class="alert alert-error" data-testid="comment-error">
          <span>{commentError}</span>
        </div>
      {/if}
      <div class="flex gap-2">
        <button
          type="submit"
          class="btn btn-xs btn-primary"
          disabled={submitting}
          data-testid="comment-submit"
        >
          {#if submitting}
            <span class="loading loading-spinner"></span>
          {:else}
            {m.comment_submit()}
          {/if}
        </button>
        <button
          class="btn btn-xs"
          type="button"
          data-testid="comment-cancel"
          onclick={cancelCommenting}
        >
          {m.form_cancel()}
        </button>
      </div>
    </form>
  {/if}

  <div class="mt-2 flex flex-col gap-2" data-testid="comments-list">
    {#each comments as comment (comment.id)}
      <PostCommentView
        postId={post.id}
        {comment}
        {timezone}
        onEdited={onCommentEdited}
        onDeleted={onCommentDeleted}
      />
    {/each}
  </div>

  {#if hasMore}
    <div class="mt-2 flex flex-col items-start gap-2">
      {#if loadMoreError}
        <div role="alert" class="alert alert-error">
          <span>{loadMoreError}</span>
        </div>
      {/if}
      <button
        class="btn btn-sm"
        type="button"
        data-testid="comments-load-more"
        aria-busy={loadMoreBusy ? "true" : "false"}
        onclick={loadMore}
        onkeydown={handleLoadMoreKey}
        disabled={loadMoreBusy}
      >
        {#if loadMoreBusy}
          <span class="loading loading-spinner"></span>
        {:else}
          {m.comments_load_more()}
        {/if}
      </button>
    </div>
  {/if}
</div>
