<script lang="ts">
  import "../app.css"
  import { updated } from "$app/state"
  import * as Sidebar from "$lib/components/ui/sidebar/index.js"
  import SiteHeader from "$lib/components/site-header.svelte"
  import AppSidebar from "$lib/components/app-sidebar.svelte"
  import { Toaster } from "$lib/components/ui/sonner/index.js";

  let { children } = $props()
</script>

<Toaster />

<Sidebar.Provider
  style="--sidebar-width: calc(var(--spacing) * 72); --header-height: calc(var(--spacing) * 12);"
>
  <AppSidebar variant="inset" />
  <Sidebar.Inset>
    <SiteHeader />
    <div class="flex flex-1 flex-col">
      <div class="@container/main flex flex-1 flex-col gap-2">
        <div class="flex flex-col gap-4 py-4 md:gap-6 md:py-6 px-4 md:px-8">
          {@render children?.()}
        </div>
      </div>
    </div>
  </Sidebar.Inset>
</Sidebar.Provider>

{#if updated.current}
  <div class="toast">
    <p>
      A new version of the app is available

      <button onclick={() => location.reload()}> reload the page </button>
    </p>
  </div>
{/if}
