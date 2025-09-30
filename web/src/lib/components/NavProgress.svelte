<script lang="ts">
  import { navigating } from "$app/state"

  let active = $state(false)
  let fadingOut = $state(false)

  const START_DELAY_MS = 500

  function start() {
    active = true
  }

  function done() {
    if (!active) {
      return
    }
    active = false
    fadingOut = true
    setTimeout(() => {
      fadingOut = false
    }, 300)
  }

  $effect(() => {
    if (navigating.to) {
      const t = setTimeout(start, START_DELAY_MS)
      return () => {
        clearTimeout(t)
      }
    } else {
      done()
    }
  })
</script>

<div class="bar-container fixed top-0 w-full">
  {#if active || fadingOut}
    <div class={["bar", active ? "animating-in" : "animating-out"]}></div>
  {/if}
</div>

<style>
  .bar-container {
    z-index: 1000;
  }

  .bar {
    height: 2px;
    background-color: var(--color-primary);
  }

  .animating-in {
    transform: scaleX(0);
    transform-origin: 0 50%;
    animation-duration: 15s;
    animation-iteration-count: 1;
    animation-name: fill-progress;
    animation-timing-function: cubic-bezier(0.2, 0.6, 0.15, 0.99);
    animation-fill-mode: forwards;
  }

  .animating-out {
    transform: scaleX(1);
    transform-origin: 0 50%;
    opacity: 1;
    animation: fadeOut 0.3s ease forwards;
  }

  @keyframes fill-progress {
    0% {
      transform: scaleX(0);
    }
    100% {
      transform: scaleX(0.99);
    }
  }
</style>
