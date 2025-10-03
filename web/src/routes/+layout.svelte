<script lang="ts">
  import "../app.css"
  import { type LayoutProps } from "./$types"
  import { onMount } from "svelte"
  import { ThemeHandler } from "$lib/utils/ThemeHandler"
  import { ClientEnv } from "$lib/utils/ClientEnv"
  import NavProgress from "$lib/components/NavProgress.svelte"
  import { prefs } from "$lib/state"

  const { children, data }: LayoutProps = $props()

  prefs.set(data.prefs)

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
