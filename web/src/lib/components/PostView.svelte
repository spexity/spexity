<script lang="ts">
  import { authManager } from "$lib/auth"
  import CommunityName from "$lib/components/CommunityName.svelte"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import Editor from "$lib/components/Editor.svelte"
  import type { CommentPage, CommentView, PostView, Prefs, SortPreference } from "$lib/model/types"
  import { DateFormatter } from "$lib/utils/DateFormatter"
  import { EditorUtils } from "$lib/utils/EditorUtils"
  import { HttpError } from "$lib/utils/HttpClient"
  import { m } from "$lib/paraglide/messages.js"
  import PostCommentView from "$lib/components/PostCommentView.svelte"
  import GatedFeature from "$lib/components/GatedFeature.svelte"
  import { ClientEnv } from "$lib/utils/ClientEnv"

  interface PostViewProps {
    post: PostView
    prefs: Prefs
    comments: CommentPage
    currentContributorId?: string
  }

  const { post, prefs, comments: initialComments, currentContributorId }: PostViewProps = $props()
  const formattedDateTime = DateFormatter.formatUtcIsoAbsolute(
    post.createdAt,
    prefs.timezone,
    prefs.locale,
  )

  let comments = $state<CommentView[]>(initialComments.items)
  let commentsMeta = $state({
    page: initialComments.page,
    pageSize: initialComments.pageSize,
  })
  let commentsCount = $state(post.commentsCount)
  let commentsOrder = $state(prefs.commentsOrder)

  let commenting = $state(false)
  let submitting = $state(false)
  let commentError = $state<string | undefined>()
  let editorRef = $state<Editor>()

  let loadMoreBusy = $state(false)
  let loadMoreError = $state<string | undefined>()

  const hasMore = $derived(comments.length < commentsCount)
  const showSortControl = $derived(commentsCount > 1)

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
        commentError = m.error_empty()
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
          contributor: authManager.userAccount!.contributor,
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
        `/api/posts/${post.id}/comments?page=${nextPage}&pageSize=${commentsMeta.pageSize}&order=${commentsOrder}`,
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

  const handleSortChange = async (newPreference: SortPreference) => {
    if (commentsOrder === newPreference) return

    commentsOrder = newPreference
    ClientEnv.setCommentsOrder(newPreference)

    commentsMeta = { page: 1, pageSize: commentsMeta.pageSize }
    loadMoreError = undefined

    try {
      const response = await authManager.httpClient.get<CommentPage>(
        `/api/posts/${post.id}/comments?page=1&pageSize=${commentsMeta.pageSize}&order=${newPreference}`,
      )
      comments = response.items
      commentsMeta = {
        page: response.page,
        pageSize: response.pageSize,
      }
    } catch {
      loadMoreError = m.comment_error_failed()
    }
  }
</script>

<div class="flex flex-col" data-testid="post-view">
  <div class="flex flex-row justify-between">
    <span class="text-lg font-medium">
      <CommunityName community={post.community} />
    </span>
    <div class="flex flex-wrap items-center gap-1 text-xs">
      <ContributorHandle contributor={post.contributor} testIdQualifier={post.id} showAvatar />
      <span class="spx-text-subtle">•</span>
      <span class="spx-text-subtle">{formattedDateTime}</span>
    </div>
  </div>
  <div class="mt-4">
    <h2 class="text-2xl font-medium" data-testid="post-subject">{post.subject}</h2>
    <!-- eslint-disable-next-line svelte/no-at-html-tags -->
    <div class="tiptap my-4">{@html post.bodyHtml}</div>
  </div>
  <div class="divider m-0"></div>
  <div class="flex items-center justify-between">
    <div class="flex items-center gap-3">
      <div class="spx-text-subtle text-xs" data-testid="comments-count">
        {m.comments_count({ count: commentsCount })}
      </div>
      {#if showSortControl}
        <div class="join" data-testid="comments-sort-control">
          <button
            class={["btn join-item btn-xs", commentsOrder === "asc" && "btn-active"]}
            data-testid="comments-sort-asc"
            type="button"
            aria-pressed={commentsOrder === "asc"}
            onclick={() => handleSortChange("asc")}
          >
            {m.sort_oldest_first()}
          </button>
          <button
            class={["btn join-item btn-xs", commentsOrder === "desc" && "btn-active"]}
            data-testid="comments-sort-desc"
            type="button"
            aria-pressed={commentsOrder === "desc"}
            onclick={() => handleSortChange("desc")}
          >
            {m.sort_newest_first()}
          </button>
        </div>
      {/if}
    </div>
    <button
      class={["btn btn-sm", commenting && "invisible"]}
      data-testid="new-comment-button"
      onclick={startCommenting}>{m.comment_action()}</button
    >
  </div>
  {#if commenting}
    {#if authManager.userAccount?.verifiedHuman}
      <form class="my-2 flex flex-col gap-2" onsubmit={handleCreateComment} autocomplete="off">
        <Editor
          bind:this={editorRef}
          id="new-comment"
          dataTestId="new-comment-editor"
          mode="comment"
        />
        {#if commentError}
          <div role="alert" class="alert alert-error" data-testid="new-comment-error">
            <span>{commentError}</span>
          </div>
        {/if}
        <div class="flex gap-2">
          <button
            type="submit"
            class="btn btn-xs btn-primary"
            disabled={submitting}
            data-testid="new-comment-submit"
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
            data-testid="new-comment-cancel"
            onclick={cancelCommenting}
          >
            {m.form_cancel()}
          </button>
        </div>
      </form>
    {:else}
      <GatedFeature mode="small" oncancel={cancelCommenting} />
    {/if}
  {/if}

  <div class="spx-cards-list" data-testid="comments-list">
    {#each comments as comment (comment.id)}
      <PostCommentView
        postId={post.id}
        {comment}
        {prefs}
        {currentContributorId}
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
          <span data-testid="comments-loading-more" class="loading loading-spinner"></span>
        {:else}
          {m.load_more()}
        {/if}
      </button>
    </div>
  {/if}
</div>
