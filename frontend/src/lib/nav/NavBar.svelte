<script lang="ts">
  interface Props {
    name: string
    url: string
  }

  let props: Props = $props()
</script>

<nav>
  <a href="/html/">HTML</a> |
  <a href="/css/">CSS</a> |
  <a href="/js/">JavaScript</a> |
  <a href="/{props.url}/">{props.name}</a>
</nav>
