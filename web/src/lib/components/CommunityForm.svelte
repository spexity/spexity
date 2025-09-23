<script lang="ts">
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"
  import { authManager } from "$lib/auth"
  import { goto } from "$app/navigation"
  import { type CommunityRef } from "$lib/model/types"

  let submitting = $state<boolean>(false)
  let errorMessage = $state<string>()

  const handleSubmit = async (event: SubmitEvent) => {
    try {
      submitting = true
      errorMessage = undefined
      const data = CsrFormHandler.onsubmit(event)
      const name = data.get("name") as string
      const conformToTermsAndConditions = data.get("conformToTermsAndConditions") === "on"
      const community = await authManager.httpClient.post<CommunityRef>("/api/communities", {
        name,
        conformToTermsAndConditions,
      })
      await goto(`/communities/${community.id}`)
    } catch (err) {
      console.error("error create", err)
      errorMessage = "Could not create community"
    } finally {
      submitting = false
    }
  }
</script>

<form class="w-full" onsubmit={handleSubmit} autocomplete="off">
  <fieldset class="fieldset">
    <label class="label" for="name">Name</label>
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
      <label class="label" for="conformToTermsAndConditions">
        <input
          id="conformToTermsAndConditions"
          name="conformToTermsAndConditions"
          type="checkbox"
          class="checkbox"
          required
        />
        <span class="label-text">
          This community details conform to Spexity
          <a href="/terms-and-conditions" class="link" target="_blank">terms and conditions</a>.
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
      Start Community
    {/if}
  </button>
</form>
