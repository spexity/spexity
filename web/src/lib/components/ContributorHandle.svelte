<script lang="ts">
  import type { ContributorRef } from "$lib/model/types"
  import { resolve } from "$app/paths"

  interface ContributorHandleProps {
    contributor: ContributorRef
  }

  const { contributor }: ContributorHandleProps = $props()

  const contributorName = contributor.handle.substring(0, contributor.handle.indexOf("#"))
  const contributorDiscriminator = contributor.handle.substring(contributorName.length)
</script>

<a href={resolve(`/contributors/${contributor.id}`)}>
  {contributorName}<span class="text-base-content/66">{contributorDiscriminator}</span>
</a>
