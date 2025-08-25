import adapter from "@sveltejs/adapter-node"
import { vitePreprocess } from "@sveltejs/vite-plugin-svelte"
import { getCommitSha } from "./scripts/git-commit-sha.js"

/** @type {import("@sveltejs/kit").Config} */
const config = {
  // Consult https://svelte.dev/docs/kit/integrations
  // for more information about preprocessors
  preprocess: vitePreprocess(),
  kit: {
    adapter: adapter(),
    version: {
      name: getCommitSha(),
      pollInterval: 60000,
    },
  },
}

export default config
