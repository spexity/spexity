<script lang="ts">
  import { resolve } from "$app/paths"
  import { auth } from "$lib/state"
  import { CsrFormHandler } from "$lib/utils/CsrFormHandler"
  import { m } from "$lib/paraglide/messages.js"
  import { bgColors } from "$lib/utils/Utils"

  interface AccountFormProps {
    initAlias?: string
    initDiscriminator?: string
    initAvatarText: string
    initAvatarBgColor: string
    onsuccess: () => void
    mode?: "register" | "update"
  }

  const {
    initAlias,
    initDiscriminator,
    initAvatarText,
    initAvatarBgColor,
    onsuccess,
    mode,
  }: AccountFormProps = $props()
  let submitting = $state<boolean>(false)
  let errorMessage = $state<string>()
  let alias = $state<string>(initAlias ?? "")
  let avatarText = $state<string>(initAvatarText)
  let avatarBgColor = $state<string>(initAvatarBgColor)
  let avatarTextError = $state<string>()
  const emojiSegmenter = new Intl.Segmenter("en", { granularity: "grapheme" })
  const emojiRegex = /\p{Extended_Pictographic}/u

  $effect(() => {
    const graphemes = [...emojiSegmenter.segment(avatarText)]
    avatarText = graphemes
      .slice(0, 2)
      .filter((g) => emojiRegex.test(g.segment))
      .map((g) => g.segment)
      .join("")
  })

  const validateEmojis = (value: string): boolean => {
    const graphemes = [...emojiSegmenter.segment(value)]
    if (graphemes.length !== 2) {
      avatarTextError = "Please enter exactly 2 emojis"
      return false
    }
    // Check if characters are emojis (unicode ranges for emoji)
    const emojiRegex = /^[\p{Emoji}\p{Emoji_Component}]+$/u
    if (!emojiRegex.test(value)) {
      avatarTextError = "Please enter only emojis"
      return false
    }
    avatarTextError = undefined
    return true
  }

  const handleSubmit = async (event: SubmitEvent) => {
    try {
      submitting = true
      errorMessage = undefined
      const data = CsrFormHandler.onsubmit(event)
      const alias = data.get("alias") as string
      const acceptTermsAndConditions = data.get("acceptTermsAndConditions") as string
      const avatarText = data.get("avatarText") as string
      const avatarBgColor = data.get("avatarBgColor") as string

      if (!validateEmojis(avatarText)) {
        submitting = false
        return
      }
      if (mode === "update") {
        await auth.updateUserAccount(alias, avatarText, avatarBgColor)
      } else {
        await auth.registerUserAccount(
          alias,
          avatarText,
          avatarBgColor,
          acceptTermsAndConditions === "on",
        )
      }
      onsuccess()
    } catch {
      errorMessage = m.error_register_failed()
    } finally {
      submitting = false
    }
  }
</script>

<form onsubmit={handleSubmit} autocomplete="off">
  <fieldset class="fieldset w-sm rounded-box border border-base-300 bg-base-200 p-4">
    <legend class="fieldset-legend text-lg">{m.form_profile_legend()}</legend>

    <label class="label" for="alias">{m.form_alias_label()}</label>
    <div class="input w-full">
      <input
        id="alias"
        name="alias"
        type="text"
        required
        minlength="3"
        maxlength="20"
        bind:value={alias}
        placeholder={m.form_alias_placeholder()}
      />
    </div>
    <p class="label">{m.form_alias_description()}</p>

    <label class="label mt-4" for="avatarText">Avatar emojis</label>
    <div class="input w-full">
      <input
        id="avatarText"
        name="avatarText"
        type="text"
        required
        placeholder="👀🍯"
        bind:value={avatarText}
        oninput={() => validateEmojis(avatarText)}
      />
    </div>
    {#if avatarTextError}
      <p class="label text-sm text-error">{avatarTextError}</p>
    {:else}
      <p class="label">Choose 2 emojis for your avatar</p>
    {/if}

    <label class="label mt-4" for="avatarBgColor">Avatar background color</label>
    <div class="flex flex-wrap gap-2">
      {#each bgColors as color (color)}
        <label class="cursor-pointer">
          <input
            type="radio"
            name="avatarBgColor"
            value={color}
            bind:group={avatarBgColor}
            class="sr-only"
          />
          <div
            class="h-10 w-10 rounded-full border-2 transition-all"
            class:border-primary={avatarBgColor === color}
            class:border-base-300={avatarBgColor !== color}
            style="background-color: {color}"
          ></div>
        </label>
      {/each}
    </div>

    <div class="mt-4">
      <p class="label">Preview</p>
      <div class="flex items-center gap-2">
        <a
          href="#"
          onclick={(event) => {
            event.preventDefault()
          }}
        >
          <span class="spx-avatar-badge" style="background-color: {avatarBgColor}"
            >{avatarText || "👀🍯"}</span
          >
          {alias || "Alias"}<span class="spx-text-subtle"
            >#{alias === initAlias ? initDiscriminator : "0000"}</span
          >
        </a>
      </div>
    </div>
    {#if mode !== "update"}
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
            {m.form_terms_prefix()}
            <a
              class="link"
              href={resolve("/terms-and-conditions")}
              target="_blank"
              rel="noopener noreferrer"
            >
              {m.legal_terms_link()}
            </a>.
          </span>
        </label>
      </div>
    {/if}
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
      {m.form_save()}
    {/if}
  </button>
</form>
