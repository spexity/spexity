import { type Page } from "@playwright/test"

export class HomePage {
  private readonly page: Page


  constructor(page: Page) {
    this.page = page
  }

}
