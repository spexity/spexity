<script lang="ts">
  import "../app.css"
  import { updated } from "$app/state"
  import * as Sidebar from "$lib/components/ui/sidebar/index.js"
  import SiteHeader from "$lib/components/site-header.svelte"
  import AppSidebar from "$lib/components/app-sidebar.svelte"
  import { Toaster } from "$lib/components/ui/sonner/index.js"
  import { Button } from "$lib/components/ui/button"
  import RocketIcon from "@tabler/icons-svelte/icons/rocket"

  let { children } = $props()
</script>

<Toaster />
<Sidebar.Provider
  style="--sidebar-width: calc(var(--spacing) * 72); --header-height: calc(var(--spacing) * 12);"
>
  <AppSidebar variant="inset" />
  <Sidebar.Inset>
    <SiteHeader>
      {#if updated.current}
        <RocketIcon />
        A new version of the app is available
        <Button variant="secondary" onclick={() => location.reload()}>Reload</Button>
      {/if}
    </SiteHeader>
    <div class="flex flex-1 flex-col">
      <div class="@container/main flex flex-1 flex-col gap-2">
        <div class="flex flex-col gap-4 px-4 py-4 md:gap-6 md:px-8 md:py-6">
          {@render children?.()}
        </div>
      </div>
    </div>
  </Sidebar.Inset>
</Sidebar.Provider>
