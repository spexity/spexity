<script lang="ts">
  import type { PageProps } from "./$types"
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"
  import { authManager } from "$lib/auth"
  import { goto } from "$app/navigation"
  import { type PostPreview } from "$lib/model/types"
  import Editor from "$lib/components/Editor.svelte"

  const { data }: PageProps = $props()
  const community = data.community

  let submitting = $state<boolean>(false)
  let errorMessage = $state<string>()
  let editorRef = $state<Editor>()

  const handleSubmit = async (event: SubmitEvent) => {
    try {
      submitting = true
      errorMessage = undefined
      const data = CsrFormHandler.onsubmit(event)
      const subject = data.get("subject") as string
      const body = editorRef?.getValue()
      const post = await authManager.httpClient.post<PostPreview>("/api/posts", {
        communityId: community.id,
        subject,
        body,
      })
      await goto(`/posts/${post.id}`)
    } catch {
      errorMessage = "Could not post"
    } finally {
      submitting = false
    }
  }
</script>

<div class="text-2xl">
  New Post to {community.name}
</div>
<form class="w-full" onsubmit={handleSubmit} autocomplete="off">
  <fieldset class="fieldset">
    <label class="label" for="subject">Subject</label>
    <input
      id="subject"
      name="subject"
      type="text"
      class="input w-full lg:w-1/2"
      required
      minlength="10"
      maxlength="512"
    />

    <label class="label" for="body">Body</label>
    <Editor bind:this={editorRef} />
  </fieldset>
  {#if errorMessage}
    <div role="alert" class="alert-soft alert alert-error">
      <span>{errorMessage}</span>
    </div>
  {/if}
  <button type="submit" class="btn mt-4 btn-sm btn-primary" disabled={submitting}>
    {#if submitting}
      <span class="loading loading-spinner"></span>
    {:else}
      Create Post
    {/if}
  </button>
</form>
