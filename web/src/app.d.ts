// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces

import type { OrderPref } from "$lib/model/types"
import type { Theme } from "$lib/utils/ThemeHandler"

declare global {
  namespace App {
    // interface Error {}
    interface Locals {
      timezone: string
      locale: string
      commentsOrder: OrderPref
      theme: Theme
      contributorId?: string
    }
    // interface PageData {}
    // interface PageState {}
    // interface Platform {}
  }
}

export {}
