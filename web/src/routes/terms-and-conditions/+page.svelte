<script lang="ts">
  import { m } from "$lib/paraglide/messages.js"
</script>

<svelte:head>
  <title>{m.legal_terms_title()} | Spexity</title>
</svelte:head>

<section class="min-h-screen bg-base-200 py-16">
  <div class="mx-auto w-full max-w-4xl px-6">
    <div class="card bg-base-100 shadow-xl">
      <div class="card-body space-y-12">
        <header class="space-y-4">
          <h1 class="card-title text-4xl">{m.legal_terms_title()}</h1>
          <p class="text-sm text-base-content/70">{m.legal_terms_updated()}</p>
          <p class="text-base text-base-content/80">
            {m.legal_terms_intro()}
          </p>
        </header>

        <div class="divider">{m.legal_terms_section_responsibilities()}</div>

        <div class="space-y-10 text-base leading-relaxed">
          <article class="space-y-3">
            <h2 class="text-2xl font-semibold text-base-content">{m.legal_terms_section_eligibility_title()}</h2>
            <p>
              {m.legal_terms_section_eligibility_content()}
            </p>
          </article>

          <article class="space-y-3">
            <h2 class="text-2xl font-semibold text-base-content">{m.legal_terms_section_content_title()}</h2>
            <p>
              {m.legal_terms_section_content_content()}
            </p>
          </article>

          <article class="space-y-3">
            <h2 class="text-2xl font-semibold text-base-content">{m.legal_terms_section_changes_title()}</h2>
            <p>
              {m.legal_terms_section_changes_content()}
            </p>
          </article>
        </div>
      </div>
    </div>
  </div>
</section>
