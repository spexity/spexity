<script lang="ts">
  import { updated } from "$app/state"

  let { children } = $props()
</script>

{@render children?.()}

{#if updated.current}
  <div class="toast">
    <p>
      A new version of the app is available

      <button onclick={() => location.reload()}> reload the page </button>
    </p>
  </div>
{/if}
