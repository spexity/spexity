<script lang="ts">
  import type { ContributorRef } from "$lib/model/types"
  import { resolve } from "$app/paths"

  interface ContributorHandleProps {
    contributor: ContributorRef
    testIdQualifier: string
    showAvatar?: boolean
  }

  const { contributor, testIdQualifier, showAvatar }: ContributorHandleProps = $props()

  const contributorName = contributor.handle.substring(0, contributor.handle.indexOf("#"))
  const contributorDiscriminator = contributor.handle.substring(contributorName.length)
</script>

<a
  href={resolve(`/contributors/${contributor.id}`)}
  data-testid={`contributor-${contributor.id}-${testIdQualifier}`}
>
  {#if showAvatar}
    <span class="spx-avatar-badge" style="background-color: {contributor.avatarBgColor}"
      >{contributor.avatarText}</span
    >
  {/if}
  {contributorName}<span class="spx-text-subtle">{contributorDiscriminator}</span>
</a>
