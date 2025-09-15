<script lang="ts">
  import type { CommunityRef } from "$lib/model/types"

  interface CommunityNameProps {
    community: CommunityRef
  }

  const { community }: CommunityNameProps = $props()
</script>

<a href="/communities/{community.id}">{community.name}</a>
