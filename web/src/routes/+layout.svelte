<script lang="ts">
  import "../app.css"
  import { onMount } from "svelte"
  import favicon from "$lib/assets/favicon.svg"
  import { ThemeHandler } from "$lib/utils/ThemeHandler"
  import { ClientEnv } from "$lib/utils/ClientEnv"

  const { children } = $props()

  onMount(() => {
    ClientEnv.setup()
    return ThemeHandler.handle()
  })
</script>

<svelte:head>
  <link rel="icon" href={favicon} />
</svelte:head>

{@render children?.()}
