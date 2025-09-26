<script lang="ts">
  import type { CommunityRef } from "$lib/model/types"
  import { resolve } from "$app/paths"

  interface CommunityNameProps {
    community: CommunityRef
  }

  const { community }: CommunityNameProps = $props()
</script>

<a href={resolve(`/communities/${community.id}`)}>{community.name}</a>
