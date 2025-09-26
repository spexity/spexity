<script lang="ts">
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"
  import { authManager } from "$lib/auth"
  import { goto } from "$app/navigation"
  import { type PostPreview } from "$lib/model/types"
  import Editor from "$lib/components/Editor.svelte"
  import { EditorUtils } from "$lib/utils/EditorUtils"
  import { m } from "$lib/paraglide/messages.js"
  import { resolve } from "$app/paths"

  interface PostFormProps {
    communityId: string
  }

  const { communityId }: PostFormProps = $props()

  let submitting = $state<boolean>(false)
  let errorMessage = $state<string>()
  let editorRef = $state<Editor>()

  const handleSubmit = async (event: SubmitEvent) => {
    try {
      submitting = true
      errorMessage = undefined
      const data = CsrFormHandler.onsubmit(event)
      const subject = data.get("subject") as string
      const conformToTermsAndConditions = data.get("conformToTermsAndConditions") as string
      const body = editorRef?.getValue()
      if (!EditorUtils.hasMeaningfulText(body)) {
        errorMessage = m.error_post_empty()
        return
      }
      const post = await authManager.httpClient.post<PostPreview>("/api/posts", {
        communityId,
        subject,
        body,
        conformToTermsAndConditions: conformToTermsAndConditions === "on",
      })
      await goto(resolve(`/posts/${post.id}`))
    } catch (err) {
      console.error("error posting", err)
      errorMessage = m.error_post_failed()
    } finally {
      submitting = false
    }
  }
</script>

<form class="w-full" onsubmit={handleSubmit} autocomplete="off">
  <fieldset class="fieldset">
    <label class="label" for="subject">{m.form_subject_label()}</label>
    <input
      id="subject"
      name="subject"
      type="text"
      class="input w-full lg:w-1/2"
      required
      minlength="10"
      maxlength="512"
    />

    <label id="bodyLabel" class="label" for="body">{m.form_body_label()}</label>
    <Editor bind:this={editorRef} id="body" labelledBy="bodyLabel" />
    <div class="form-control">
      <label class="label" for="conformToTermsAndConditions">
        <input
          id="conformToTermsAndConditions"
          name="conformToTermsAndConditions"
          type="checkbox"
          class="checkbox"
          required
        />
        <span class="label-text">
          {m.form_compliance_prefix()}
          <a href={resolve("/terms-and-conditions")} class="link" target="_blank"
            >{m.legal_terms_link()}</a
          >
          {m.form_compliance_suffix()}
        </span>
      </label>
    </div>
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
      {m.post_create_submit()}
    {/if}
  </button>
</form>
