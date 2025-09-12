<script lang="ts">
  import { onMount } from "svelte"
  import { authManager } from "$lib/auth"

  onMount(async () => {
    try {
      await authManager.signInCallback()
    } catch (err) {
      console.error("Could not fathom callback", err)
    }
  })
</script>

<div class="flex h-screen flex-col items-center justify-center">
  <span class="loading loading-lg loading-spinner"></span>
  <p class="mt-4">Redirecting...</p>
</div>
