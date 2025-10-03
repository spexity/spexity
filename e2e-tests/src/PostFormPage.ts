import { expect, type Page } from "@playwright/test";

export class PostFormPage {
  private readonly page: Page;

  constructor(page: Page) {
    this.page = page;
  }

  async waitForPageLoad() {
    await expect(this.page.locator("#subject")).toBeVisible();
  }

  getSubjectInput() {
    return this.page.locator("#subject");
  }

  getBodyEditor() {
    return this.page.getByTestId("post-body-editor");
  }

  getTermsCheckbox() {
    return this.page.locator("#acceptTermsAndConditions");
  }

  getSubmitButton() {
    return this.page.getByRole("button", { name: "Create Post" });
  }

  async createPost(subject: string, body: string) {
    await this.getSubjectInput().fill(subject);
    await this.getBodyEditor().click();
    await this.page.keyboard.type(body);
    await this.getTermsCheckbox().check();
    await this.getSubmitButton().click();
    await this.page.waitForURL("**/posts/*");
  }
}
