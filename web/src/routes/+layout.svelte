<script lang="ts">
  import "../app.css"
  import { onMount } from "svelte"
  import { ThemeHandler } from "$lib/utils/ThemeHandler"
  import { ClientEnv } from "$lib/utils/ClientEnv"
  import NavProgress from "$lib/components/NavProgress.svelte"

  const { children } = $props()

  onMount(() => {
    ClientEnv.setup()
    return ThemeHandler.handle()
  })
</script>

<svelte:head>
  <link rel="icon" href="/logo.png" />
</svelte:head>
{@render children?.()}
<NavProgress />
