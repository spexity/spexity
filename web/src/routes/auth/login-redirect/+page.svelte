<script lang="ts">
  import { onMount } from "svelte"
  import { auth } from "$lib/state"
  import { m } from "$lib/paraglide/messages.js"

  onMount(async () => {
    try {
      await auth.signInCallback()
    } catch (err) {
      console.error("Could not fathom callback", err)
    }
  })
</script>

<div class="flex h-screen flex-col items-center justify-center">
  <span class="loading loading-lg loading-spinner"></span>
  <p class="mt-4">{m.loading_redirecting()}</p>
</div>
