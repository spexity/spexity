<script lang="ts">
  import type { PageProps } from "./$types"

  let props: PageProps = $props()
</script>

<div>
  this is the user {props.data.bio} and the {props.data.username}
</div>
