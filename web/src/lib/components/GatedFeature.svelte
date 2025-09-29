<script lang="ts">
  import { authManager } from "$lib/auth"
  import { m } from "$lib/paraglide/messages.js"

  interface GatedFeatureProps {
    mode?: "full" | "small"
    oncancel?: () => void
  }

  const { mode, oncancel }: GatedFeatureProps = $props()
</script>

<div
  class={[
    "flex flex-col items-center justify-center text-center",
    mode !== "small" && "min-h-[25vh] text-2xl font-semibold",
  ]}
  data-testid="gated-feature"
>
  <div class="flex flex-row justify-center">
    <span>
      {#if authManager.userAccount}
        You need to be verified as a human before proceeding.
      {:else}
        You need to sign in and verify humanity.
      {/if}
    </span>
  </div>
  {#if oncancel}
    <div>
      <button class="btn btn-xs" data-testid="gated-feature-cancel" onclick={oncancel}>
        {m.form_cancel()}
      </button>
    </div>
  {/if}
</div>
