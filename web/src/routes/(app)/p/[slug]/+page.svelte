<script lang="ts">
  import type { PageProps } from "./$types"

  let props: PageProps = $props()
</script>

<div>
  You are viewing post {props.data.id}
</div>
