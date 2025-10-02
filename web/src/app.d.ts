// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces

import type { SortPreference } from "$lib/model/types"

declare global {
  namespace App {
    // interface Error {}
    interface Locals {
      timezone: string
      locale: string
      commentsOrder: SortPreference
      contributorId?: string
    }
    // interface PageData {}
    // interface PageState {}
    // interface Platform {}
  }
}

export {}
