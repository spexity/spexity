<script lang="ts">
  import { setLocale } from "$lib/paraglide/runtime"
  import { m } from "$lib/paraglide/messages.js"
</script>

<h1>{m.hello_world({ name: "SvelteKit User" })}</h1>
<div>
  <button onclick={() => setLocale("en")}>en</button>
  <button onclick={() => setLocale("zh-cn")}>zh-cn</button>
  <button onclick={() => setLocale("ar")}>ar</button>
</div>
