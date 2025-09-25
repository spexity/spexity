<script lang="ts">
  import "../app.css"
  import { onMount } from "svelte"
  import { ThemeHandler } from "$lib/utils/ThemeHandler"
  import { ClientEnv } from "$lib/utils/ClientEnv"
  import NavProgress from "$lib/components/NavProgress.svelte"
  import { getLocale } from "$lib/paraglide/runtime"
  import { browser } from "$app/environment"

  const { children } = $props()

  // Reactive direction based on locale
  $: locale = browser ? getLocale() : 'en'
  $: isRtl = locale === 'ar'
  $: direction = isRtl ? 'rtl' : 'ltr'

  onMount(() => {
    ClientEnv.setup()
    return ThemeHandler.handle()
  })

  // Update document direction when locale changes
  $effect(() => {
    if (browser && document.documentElement) {
      document.documentElement.dir = direction
      document.documentElement.lang = locale
    }
  })
</script>

<svelte:head>
  <link rel="icon" href="/logo.png" />
</svelte:head>
{@render children?.()}
<NavProgress />
