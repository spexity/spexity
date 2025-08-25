<script lang="ts">
  import HomeIcon from "@tabler/icons-svelte/icons/home"

  import HelpIcon from "@tabler/icons-svelte/icons/help"
  import InnerShadowTopIcon from "@tabler/icons-svelte/icons/inner-shadow-top"
  import ListDetailsIcon from "@tabler/icons-svelte/icons/list-details"
  import SettingsIcon from "@tabler/icons-svelte/icons/settings"
  import UsersIcon from "@tabler/icons-svelte/icons/users"
  import NavMain from "./nav-main.svelte"
  import NavSecondary from "./nav-secondary.svelte"
  import NavUser from "./nav-user.svelte"
  import * as Sidebar from "$lib/components/ui/sidebar/index.js"
  import type { ComponentProps } from "svelte"

  const data = {
    user: {
      name: "Ham",
      email: "ham314@outlook.com",
      avatar: "",
    },
    navMain: [
      {
        title: "Home",
        url: "#",
        icon: HomeIcon,
      },
      {
        title: "Topics",
        url: "#",
        icon: ListDetailsIcon,
      },
      {
        title: "Groups",
        url: "#",
        icon: UsersIcon,
      },
    ],
    navSecondary: [
      {
        title: "Settings",
        url: "#",
        icon: SettingsIcon,
      },
      {
        title: "Get Help",
        url: "#",
        icon: HelpIcon,
      },
    ],
  }

  let { ...restProps }: ComponentProps<typeof Sidebar.Root> = $props()
</script>

<Sidebar.Root collapsible="offcanvas" {...restProps}>
  <Sidebar.Header>
    <Sidebar.Menu>
      <Sidebar.MenuItem>
        <Sidebar.MenuButton class="data-[slot=sidebar-menu-button]:!p-1.5">
          {#snippet child({ props })}
            <a href="##" {...props}>
              <InnerShadowTopIcon class="!size-5" />
              <span class="text-base font-semibold">Spexity</span>
            </a>
          {/snippet}
        </Sidebar.MenuButton>
      </Sidebar.MenuItem>
    </Sidebar.Menu>
  </Sidebar.Header>
  <Sidebar.Content>
    <NavMain items={data.navMain} />
    <NavSecondary items={data.navSecondary} class="mt-auto" />
  </Sidebar.Content>
  <Sidebar.Footer>
    <NavUser user={data.user} />
  </Sidebar.Footer>
</Sidebar.Root>
