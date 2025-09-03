<script lang="ts">
  import "../app.css"
  import { updated } from "$app/state"

  let { children } = $props()
</script>

Spexity
{#if updated.current}
  A new version of the app is available
  <button onclick={() => location.reload()}>Reload</button>
{/if}
{@render children?.()}
