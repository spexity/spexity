<script lang="ts">
  import type { TopicPreview } from "$lib/components/TopicPreview"

  interface TopicPreviewProps {
    topic: TopicPreview
  }

  const props: TopicPreviewProps = $props()
  const topic = props.topic
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
