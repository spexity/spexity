<script lang="ts">
  import "../app.css"
  import { updated } from "$app/state"
  import { onMount } from "svelte"
  import favicon from "$lib/assets/favicon.svg"
  import { APP_THEME } from "../app-theme"

  let { children } = $props()
  onMount(() => {
    let currentlyDark =
      window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches
    document.documentElement.setAttribute(
      "data-theme",
      currentlyDark ? APP_THEME.dark : APP_THEME.light,
    )

    if (window.matchMedia) {
      let listener = (e: MediaQueryListEvent) => {
        document.documentElement.setAttribute(
          "data-theme",
          e.matches ? APP_THEME.dark : APP_THEME.light,
        )
      }
      window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", listener)
      return window
        .matchMedia("(prefers-color-scheme: dark)")
        .removeEventListener("change", listener)
    }
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
