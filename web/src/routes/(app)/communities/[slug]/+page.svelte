<script lang="ts">
  import type { PageProps } from "./$types"

  let props: PageProps = $props()
  let community = props.data.community
</script>

<div>
  this is the community {community.name}
</div>
