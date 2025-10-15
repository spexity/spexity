<script lang="ts">
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"
  import { auth } from "$lib/state"
  import { goto } from "$app/navigation"
  import { resolve } from "$app/paths"
  import { type CommunityRef } from "$lib/model/types"
  import { m } from "$lib/paraglide/messages.js"

  let submitting = $state<boolean>(false)
  let errorMessage = $state<string>()

  const handleSubmit = async (event: SubmitEvent) => {
    try {
      submitting = true
      errorMessage = undefined
      const data = CsrFormHandler.onsubmit(event)
      const name = data.get("name") as string
      const acceptTermsAndConditions = data.get("acceptTermsAndConditions") === "on"
      const community = await auth.httpClient.post<CommunityRef>("/api/communities", {
        name,
        acceptTermsAndConditions,
      })
      await goto(resolve(`/communities/${community.id}`))
    } catch (err) {
      console.error("error create", err)
      errorMessage = m.community_form_error_failed()
    } finally {
      submitting = false
    }
  }
</script>

<form class="w-full" onsubmit={handleSubmit} autocomplete="off">
  <fieldset class="fieldset">
    <label class="label" for="name">{m.community_form_name_label()}</label>
    <input
      id="name"
      name="name"
      type="text"
      class="input w-full lg:w-1/2"
      required
      minlength="3"
      maxlength="64"
    />
    <div class="form-control">
      <label class="label" for="acceptTermsAndConditions">
        <input
          id="acceptTermsAndConditions"
          name="acceptTermsAndConditions"
          type="checkbox"
          class="checkbox"
          required
        />
        <span class="label-text">
          {m.community_form_terms_prefix()}
          <a href={resolve("/terms-and-conditions")} class="link" target="_blank"
            >{m.legal_terms_link()}</a
          >.
        </span>
      </label>
    </div>
  </fieldset>
  {#if errorMessage}
    <div role="alert" class="alert alert-soft alert-error">
      <span>{errorMessage}</span>
    </div>
  {/if}
  <button type="submit" class="btn mt-4 btn-sm btn-primary" disabled={submitting}>
    {#if submitting}
      <span class="loading loading-spinner"></span>
    {:else}
      {m.community_form_submit()}
    {/if}
  </button>
</form>
