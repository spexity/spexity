<script lang="ts">
  import "../app.css"
  import { type LayoutProps } from "./$types"
  import { onMount } from "svelte"
  import { ThemeHandler } from "$lib/utils/ThemeHandler"
  import { ClientEnv } from "$lib/utils/ClientEnv"
  import NavProgress from "$lib/components/NavProgress.svelte"
  import { auth, prefs } from "$lib/state"

  const { children, data }: LayoutProps = $props()

  auth.ssrAwareContributorId = data.currentContributorId
  prefs.set(data.prefs)

  onMount(() => {
    ClientEnv.setup()
  })

  $effect(() => {
    ThemeHandler.handle(prefs.theme)
  })
</script>

<svelte:head>
  <link rel="icon" href="/logo.png" />
</svelte:head>
{@render children?.()}
<NavProgress />
