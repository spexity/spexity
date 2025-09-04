<script lang="ts">
  import "../app.css"
  import { updated } from "$app/state"
  import { onMount } from "svelte"
  import favicon from "$lib/assets/favicon.svg"

  let { children } = $props()
  onMount(() => {
    document.body.dataset["theme"] = "cupcake"
  })
</script>

<svelte:head>
  <link rel="icon" href={favicon} />
</svelte:head>
<div class="app-body">
  <div class="navbar bg-base-100 shadow-sm">
    <div class="h-full flex-1">
      <a href="/">
        <img src={favicon} alt="Spexity logo" class="h-full" />
      </a>
    </div>
    <div class="flex-none">
      <ul class="menu menu-horizontal px-1">
        <li><a href="/" class="menu-active">Home</a></li>
        <li><a href="/c">Communities</a></li>
        <li><a href="/t">Topics</a></li>
      </ul>
    </div>
  </div>
  {@render children?.()}
  {#if updated.current}
    <div class="toast">
      <div class="alert alert-info">
        <span>A new version is available</span>
        <button class="btn btn-outline" onclick={() => location.reload()}>Reload</button>
      </div>
    </div>
  {/if}
</div>
