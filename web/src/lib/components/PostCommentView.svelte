<script lang="ts">
  import type { CommentView } from "$lib/model/types"
  import { tick } from "svelte"
  import { EditorUtils } from "$lib/utils/EditorUtils"
  import { auth, prefs } from "$lib/state"
  import Editor from "$lib/components/Editor.svelte"
  import { m } from "$lib/paraglide/messages.js"
  import ContributorHandle from "$lib/components/ContributorHandle.svelte"
  import { DateFormatter } from "$lib/utils/DateFormatter"

  interface PostCommentViewProps {
    postId: string
    comment: CommentView
    currentContributorId?: string
    onEdited: (comment: CommentView) => void
    onDeleted: (id: string) => void
  }

  let { postId, comment, currentContributorId, onEdited, onDeleted }: PostCommentViewProps =
    $props()
  let editing = $state(false)
  let editingSubmitting = $state(false)
  let editingError = $state<string | undefined>()
  let editingEditorRef = $state<Editor>()

  let deleteConfirming = $state(false)
  let deleting = $state(false)
  let deleteError = $state<string | undefined>()

  let formattedDateTime = DateFormatter.formatUtcIsoAbsolute(
    comment.createdAt,
    prefs.timezone,
    prefs.locale,
  )

  const startEditing = async (comment: CommentView) => {
    editingError = undefined
    deleteConfirming = false
    editing = true
    await tick()
    editingEditorRef?.setHtmlValue(comment.bodyHtml ?? "")
  }

  const cancelEditing = () => {
    editing = false
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
      editingError = m.error_empty()
      return
    }
    try {
      editingSubmitting = true
      editingError = undefined
      await auth.httpClient.patch<CommentView>(`/api/posts/${postId}/comments/${comment.id}`, {
        bodyDocument,
      })
      onEdited({
        ...comment,
        bodyHtml: bodyHtml,
        editCount: (comment.editCount ?? 0) + 1,
      })
      editing = false
    } catch {
      editingError = m.comment_error_failed()
    } finally {
      editingSubmitting = false
    }
  }

  const askDelete = () => {
    deleteError = undefined
    editing = false
    deleteConfirming = true
  }

  const cancelDelete = () => {
    deleteError = undefined
    deleteConfirming = false
  }

  const performDelete = async (comment: CommentView) => {
    try {
      deleting = true
      deleteError = undefined
      await auth.httpClient.delete(`/api/posts/${postId}/comments/${comment.id}`)
      onDeleted(comment.id)
      deleteConfirming = false
    } catch {
      deleteError = m.comment_error_failed()
    } finally {
      deleting = false
    }
  }
</script>

<article class="spx-card" data-testid={`comment-item-${comment.id}`}>
  <div class="spx-card-header text-xs">
    <div class="flex items-center">
      <ContributorHandle
        contributor={comment.contributor}
        testIdQualifier={comment.id}
        showAvatar
      />
      {#if comment.editCount && !comment.deleted}
        <span class="badge badge-ghost badge-xs" data-testid={`comment-edited-badge-${comment.id}`}>
          {m.edited()}
        </span>
      {/if}
    </div>
    <span class="spx-text-subtle">{formattedDateTime}</span>
  </div>
  {#if comment.deleted}
    <p class="spx-text-subtle text-xs" data-testid={`comment-deleted-placeholder-${comment.id}`}>
      {m.deleted_placeholder()}
    </p>
  {:else}
    {#if !editing}
      <div class="tiptap comment-tiptap" data-testid={`comment-body-${comment.id}`}>
        <!-- eslint-disable-next-line svelte/no-at-html-tags -->
        {@html comment.bodyHtml}
      </div>
    {/if}
    {#if comment.contributor.id === currentContributorId}
      {#if editing}
        <form class="flex flex-col gap-2" onsubmit={(event) => saveEdit(event, comment)}>
          <Editor
            bind:this={editingEditorRef}
            id={`comment-edit-${comment.id}`}
            dataTestId={`comment-edit-editor-${comment.id}`}
            mode="comment"
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
              class="btn btn-xs btn-primary"
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
              class="btn btn-xs"
              type="button"
              data-testid={`comment-edit-cancel-${comment.id}`}
              onclick={cancelEditing}
            >
              {m.form_cancel()}
            </button>
          </div>
        </form>
      {:else}
        <div class="flex flex-wrap gap-2">
          <button
            class="btn btn-xs"
            type="button"
            data-testid={`comment-edit-button-${comment.id}`}
            onclick={() => startEditing(comment)}
          >
            {m.edit_action()}
          </button>

          {#if deleteConfirming}
            <button
              class="btn btn-xs"
              type="button"
              data-testid={`comment-delete-cancel-${comment.id}`}
              disabled={deleting}
              onclick={() => cancelDelete()}
            >
              {m.form_cancel()}
            </button>
            <button
              class="btn btn-xs btn-error"
              type="button"
              data-testid={`comment-delete-confirm-${comment.id}`}
              disabled={deleting}
              onclick={() => performDelete(comment)}
            >
              {#if deleting}
                <span class="loading loading-spinner"></span>
              {:else}
                {m.delete_action()}
              {/if}
            </button>
          {:else}
            <button
              class="btn btn-xs"
              type="button"
              data-testid={`comment-delete-button-${comment.id}`}
              onclick={askDelete}
            >
              {m.delete_action()}
            </button>
          {/if}
        </div>
      {/if}
    {/if}
  {/if}
  {#if deleteError}
    <div role="alert" class="mt-4 alert alert-error">
      <span>{deleteError}</span>
    </div>
  {/if}
</article>
