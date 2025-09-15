<script lang="ts">
  import type { TopicPreview } from "$lib/model/types"

  interface TopicPreviewProps {
    topic: TopicPreview
  }

  const { topic }: TopicPreviewProps = $props()
</script>

<div class="flex flex-col">
  <div class="flex flex-row justify-between">
    <span class="text-xs font-medium">
      {topic.name}
    </span>
  </div>
  <div class="flex flex-row justify-end">
    <a class="btn btn-sm" href="/topics/{topic.id}">View</a>
  </div>
</div>
