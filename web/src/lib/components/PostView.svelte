<script lang="ts">
  import { tick } from "svelte"
  import { authManager } from "$lib/auth"
  import CommunityName from "$lib/components/CommunityName.svelte"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import Editor from "$lib/components/Editor.svelte"
  import type { CommentPage, CommentView, PostView } from "$lib/model/types"
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import { type EditorContent, EditorUtils } from "$lib/utils/EditorUtils"
  import { HttpError } from "$lib/utils/HttpClient"
  import { m } from "$lib/paraglide/messages.js"

  interface PostViewProps {
    post: PostView
    timezone: string
    comments: CommentPage
  }

  const { post, timezone, comments: initialComments }: PostViewProps = $props()
  const formattedDateTime = DateFormatter.formatUtcIsoAbsolute(post.createdAt, timezone)

  const EMPTY_DOC: EditorContent = { type: "doc", content: [] }

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

  let editingId = $state<string | null>(null)
  let editingSubmitting = $state(false)
  let editingError = $state<string | undefined>()
  let editingEditorRef = $state<Editor>()

  let deleteConfirmationId = $state<string | null>(null)
  let deletingId = $state<string | null>(null)
  let deleteError = $state<string | undefined>()

  let loadMoreBusy = $state(false)
  let loadMoreError = $state<string | undefined>()

  const hasMore = $derived(comments.length < commentsCount)

  const handleCreateComment = async (event: SubmitEvent) => {
    event.preventDefault()
    try {
      commentError = undefined
      const body = editorRef?.getValue() ?? EMPTY_DOC
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

  const startEditing = async (comment: CommentView) => {
    editingError = undefined
    deleteConfirmationId = null
    editingId = comment.id
    await tick()
    editingEditorRef?.setHtmlValue(comment.bodyHtml ?? "")
  }

  const cancelEditing = () => {
    editingId = null
    editingError = undefined
  }

  const saveEdit = async (event: SubmitEvent, comment: CommentView) => {
    event.preventDefault()
    if (!editingEditorRef) {
      return
    }
    const bodyDocument = editingEditorRef.getValue()
    const bodyHtml = editingEditorRef.getValueHtml()
    if (!EditorUtils.hasMeaningfulText(bodyDocument)) {
      editingError = m.comment_error_empty()
      return
    }
    try {
      editingSubmitting = true
      editingError = undefined
      await authManager.httpClient.patch<CommentView>(
        `/api/posts/${post.id}/comments/${comment.id}`,
        { bodyDocument },
      )
      comments = comments.map((current) =>
        current.id === comment.id
          ? {
              ...current,
              bodyHtml: bodyHtml,
              editCount: (current.editCount ?? 0) + 1,
            }
          : current,
      )
      editingId = null
    } catch {
      editingError = m.comment_error_failed()
    } finally {
      editingSubmitting = false
    }
  }

  const askDelete = (commentId: string) => {
    deleteError = undefined
    editingId = null
    deleteConfirmationId = commentId
  }

  const cancelDelete = () => {
    deleteError = undefined
    deleteConfirmationId = null
  }

  const performDelete = async (comment: CommentView) => {
    try {
      deletingId = comment.id
      deleteError = undefined
      await authManager.httpClient.delete(`/api/posts/${post.id}/comments/${comment.id}`)
      const deletedAt = new Date().toISOString()
      comments = comments.map((current) =>
        current.id === comment.id
          ? {
              ...current,
              deleted: true,
              deletedAt,
              bodyHtml: `<p>${m.comment_deleted_placeholder()}</p>`,
              body: null,
            }
          : current,
      )
      commentsCount = Math.max(0, commentsCount - 1)
      deleteConfirmationId = null
    } catch {
      deleteError = m.comment_error_failed()
    } finally {
      deletingId = null
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

  const formatCreatedAt = (comment: CommentView) =>
    DateFormatter.formatUtcIsoAbsolute(comment.createdAt, timezone)
</script>

<div class="flex flex-col" data-testid="post-view">
  <div class="flex flex-row justify-between">
    <span class="text-xs font-medium">
      <CommunityName community={post.community} />
    </span>
    <div class="text-xs">
      <ContributorHandle contributor={post.contributor} testIdQualifier={post.id} />
      - {formattedDateTime}
    </div>
  </div>
  <div class="mt-4">
    <h2 class="text-2xl font-medium">{post.subject}</h2>
    <!-- eslint-disable-next-line svelte/no-at-html-tags -->
    <div class="tiptap my-4">{@html post.bodyHtml}</div>
  </div>
  <div class="divider"></div>
  <div class="flex items-center justify-between">
    <div class="text-sm" data-testid="comments-count">
      {m.comments_count({ count: commentsCount })}
    </div>
    {#if commenting}
      <button
        class="btn btn-sm"
        data-testid="comment-toggle"
        onclick={() => {
          commenting = false
          commentError = undefined
        }}>{m.comment_discard()}</button
      >
    {:else}
      <button
        class="btn btn-sm btn-primary"
        data-testid="comment-toggle"
        onclick={() => {
          commenting = true
          commentError = undefined
        }}>{m.comment_button()}</button
      >
    {/if}
  </div>
  {#if commenting}
    <form class="mt-4 flex flex-col gap-4" onsubmit={handleCreateComment} autocomplete="off">
      <fieldset class="fieldset">
        <Editor bind:this={editorRef} id="new-comment" dataTestId="comment-editor" />
      </fieldset>
      {#if commentError}
        <div role="alert" class="alert alert-error" data-testid="comment-error">
          <span>{commentError}</span>
        </div>
      {/if}
      <button
        type="submit"
        class="btn self-start btn-sm btn-primary"
        disabled={submitting}
        data-testid="comment-submit"
      >
        {#if submitting}
          <span class="loading loading-spinner"></span>
        {:else}
          {m.comment_submit()}
        {/if}
      </button>
    </form>
  {/if}

  <div class="mt-2 flex flex-col gap-2" data-testid="comments-list">
    {#each comments as comment (comment.id)}
      <article
        class="rounded-lg border border-base-300 p-3"
        data-testid={`comment-item-${comment.id}`}
      >
        <div class="flex flex-col gap-2">
          <div class="flex flex-wrap items-center gap-2 text-xs">
            <ContributorHandle contributor={comment.contributor} testIdQualifier={comment.id} />
            - {formatCreatedAt(comment)}
            {#if comment.editCount && !comment.deleted}
              <span
                class="badge badge-ghost badge-xs"
                data-testid={`comment-edited-badge-${comment.id}`}
              >
                {m.comment_edited_badge()}
              </span>
            {/if}
          </div>
          {#if comment.deleted}
            <p
              class="text-sm font-medium text-base-content/66"
              data-testid={`comment-deleted-placeholder-${comment.id}`}
            >
              {m.comment_deleted_placeholder()}
            </p>
          {:else}
            <div class="tiptap" data-testid={`comment-body-${comment.id}`}>
              <!-- eslint-disable-next-line svelte/no-at-html-tags -->
              {@html comment.bodyHtml}
            </div>
            {#if comment.contributor.id === authManager.userAccount?.contributorId}
              {#if editingId === comment.id}
                <form
                  class="mt-3 flex flex-col gap-3"
                  onsubmit={(event) => saveEdit(event, comment)}
                >
                  <Editor
                    bind:this={editingEditorRef}
                    id={`comment-edit-${comment.id}`}
                    dataTestId={`comment-edit-editor-${comment.id}`}
                  />
                  {#if editingError}
                    <div
                      role="alert"
                      class="alert alert-error"
                      data-testid={`comment-edit-error-${comment.id}`}
                    >
                      <span>{editingError}</span>
                    </div>
                  {/if}
                  <div class="flex gap-2">
                    <button
                      class="btn btn-sm btn-primary"
                      type="submit"
                      disabled={editingSubmitting}
                      data-testid={`comment-save-${comment.id}`}
                    >
                      {#if editingSubmitting}
                        <span class="loading loading-spinner"></span>
                      {:else}
                        {m.form_save()}
                      {/if}
                    </button>
                    <button
                      class="btn btn-sm"
                      type="button"
                      data-testid={`comment-edit-cancel-${comment.id}`}
                      onclick={cancelEditing}
                    >
                      {m.comment_discard()}
                    </button>
                  </div>
                </form>
              {:else}
                <div class="flex flex-wrap gap-2">
                  <button
                    class="btn btn-xs"
                    type="button"
                    data-testid={`comment-edit-${comment.id}`}
                    onclick={() => startEditing(comment)}
                  >
                    {m.comment_edit()}
                  </button>

                  {#if deleteConfirmationId === comment.id}
                    <button
                      class="btn btn-xs"
                      type="button"
                      data-testid={`comment-delete-cancel-${comment.id}`}
                      disabled={deletingId === comment.id}
                      onclick={() => cancelDelete()}
                    >
                      {m.form_cancel()}
                    </button>
                    <button
                      class="btn btn-xs btn-error"
                      type="button"
                      data-testid={`comment-delete-confirm-${comment.id}`}
                      disabled={deletingId === comment.id}
                      onclick={() => performDelete(comment)}
                    >
                      {#if deletingId === comment.id}
                        <span class="loading loading-spinner"></span>
                      {:else}
                        {m.comment_delete()}
                      {/if}
                    </button>
                  {:else}
                    <button
                      class="btn btn-xs"
                      type="button"
                      data-testid={`comment-delete-${comment.id}`}
                      onclick={() => askDelete(comment.id)}
                    >
                      {m.comment_delete()}
                    </button>
                  {/if}
                </div>
              {/if}
            {/if}
          {/if}
        </div>
      </article>
    {/each}
  </div>

  {#if deleteError}
    <div role="alert" class="mt-4 alert alert-error">
      <span>{deleteError}</span>
    </div>
  {/if}

  {#if hasMore}
    <div class="mt-2 flex flex-col items-start gap-2">
      {#if loadMoreError}
        <div role="alert" class="alert alert-error">
          <span>{loadMoreError}</span>
        </div>
      {/if}
      <button
        class="btn btn-outline btn-sm"
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
