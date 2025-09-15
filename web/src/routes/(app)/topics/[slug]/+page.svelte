<script lang="ts">
  import type { PageProps } from "./$types"

  let props: PageProps = $props()
  let topic = props.data.topic
</script>

<div>
  this is the topic {topic.name}
</div>
