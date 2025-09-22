<script lang="ts">
  import { goto } from "$app/navigation"
  import { authManager } from "$lib/auth"
  import { AuthUserAccountState } from "$lib/utils/AuthManager.svelte"
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"

  let submitting = $state<boolean>(false)
  let errorMessage = $state<string>()

  $effect(() => {
    if (
      authManager.userAccountState !== AuthUserAccountState.INIT &&
      authManager.userAccountState != AuthUserAccountState.NOT_REGISTERED
    ) {
      goHome()
    }
  })

  const goHome = async () => {
    await goto("/")
  }

  const handleSubmit = async (event: SubmitEvent) => {
    try {
      submitting = true
      errorMessage = undefined
      const data = CsrFormHandler.onsubmit(event)
      const alias = data.get("alias") as string
      const acceptTermsAndConditions = data.get("acceptTermsAndConditions") as string
      await authManager.registerUserAccount(alias, acceptTermsAndConditions === "on")
      await goto("/")
    } catch {
      errorMessage = "Could not register"
    } finally {
      submitting = false
    }
  }
</script>

<div class="flex h-screen flex-col items-center justify-center">
  {#if authManager.userAccountState === AuthUserAccountState.INIT}
    <span class="loading loading-lg loading-spinner"></span>
    <p class="mt-4">Redirecting...</p>
  {:else}
    <form onsubmit={handleSubmit} autocomplete="off">
      <fieldset class="fieldset w-sm rounded-box border border-base-300 bg-base-200 p-4">
        <legend class="fieldset-legend text-lg">Profile</legend>

        <label class="label" for="alias">Pick an alias</label>
        <div class="input w-full">
          <input
            id="alias"
            name="alias"
            type="text"
            required
            minlength="3"
            maxlength="20"
            placeholder="Alias"
          />
        </div>
        <p class="label">Your publicly visible name</p>
        <div class="form-control mt-4">
          <label class="label" for="acceptTermsAndConditions">
            <input
              id="acceptTermsAndConditions"
              name="acceptTermsAndConditions"
              type="checkbox"
              class="checkbox"
              required
            />
            <span class="label-text">
              I am a human and I accept the
              <a
                class="link"
                href="/terms-and-conditions"
                target="_blank"
                rel="noopener noreferrer"
              >
                terms and conditions
              </a>.
            </span>
          </label>
        </div>
        {#if errorMessage}
          <div role="alert" class="alert-soft alert alert-error">
            <span>{errorMessage}</span>
          </div>
        {/if}
      </fieldset>
      <button type="submit" class="btn mt-4 btn-primary" disabled={submitting}>
        {#if submitting}
          <span class="loading loading-spinner"></span>
        {:else}
          Save
        {/if}
      </button>
    </form>
  {/if}
</div>
