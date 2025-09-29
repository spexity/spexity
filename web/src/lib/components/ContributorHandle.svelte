<script lang="ts">
  import type { ContributorRef } from "$lib/model/types"
  import { resolve } from "$app/paths"

  interface ContributorHandleProps {
    contributor: ContributorRef
    testIdQualifier: string
  }

  const { contributor, testIdQualifier }: ContributorHandleProps = $props()

  const contributorName = contributor.handle.substring(0, contributor.handle.indexOf("#"))
  const contributorDiscriminator = contributor.handle.substring(contributorName.length)
</script>

<a
  href={resolve(`/contributors/${contributor.id}`)}
  data-testid={`contributor-${contributor.id}-${testIdQualifier}`}
>
  {contributorName}<span class="text-subtle">{contributorDiscriminator}</span>
</a>
