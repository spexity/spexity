import { expect, type Locator, type Page } from "@playwright/test"

export class Comment {
  readonly page: Page
  private locator: Locator

  constructor(page: Page, locator: Locator) {
    this.page = page
    this.locator = locator
  }

  getBody() {
    return this.locator.locator("[data-testid^=\"comment-body-\"]")
  }

  getEditedBadge() {
    return this.locator.locator("[data-testid^=\"comment-edited-badge-\"]")
  }

  getDeletedPlaceholder() {
    return this.locator.locator("[data-testid^=\"comment-deleted-placeholder-\"]")
  }

  private getEditButton() {
    return this.locator.locator("[data-testid^=\"comment-edit-button-\"]")
  }

  private getEditEditor() {
    return this.locator.locator("[data-testid^=\"comment-edit-editor-\"]")
  }

  private getEditSaveButton() {
    return this.locator.locator("[data-testid^=\"comment-save-\"]")
  }

  private getEditCancelButton() {
    return this.locator.locator("[data-testid^=\"comment-edit-cancel-\"]")
  }

  getEditError() {
    return this.locator.locator("[data-testid^=\"comment-edit-error-\"]")
  }

  async openEdit() {
    const button = this.getEditButton()
    await expect(button).toBeVisible()
    await button.click()
    await expect(this.getEditEditor()).toBeVisible()
  }

  async appendToEditor(value: string) {
    const editor = this.getEditEditor()
    await expect(editor).toBeVisible()
    await editor.click()
    await this.page.keyboard.type(value)
  }

  async saveEdit() {
    const button = this.getEditSaveButton()
    await button.click()
    await expect(this.getEditEditor()).toHaveCount(0)
  }

  async saveEditExpectingError() {
    const button = this.getEditSaveButton()
    await button.click()
    await expect(this.getEditError()).toBeVisible()
  }

  async cancelEdit() {
    const button = this.getEditCancelButton()
    if (await button.count()) {
      await button.click()
      await expect(this.getEditEditor()).toHaveCount(0)
    }
  }

  private getDeleteButton() {
    return this.locator.locator("[data-testid^=\"comment-delete-button-\"]")
  }

  private getDeleteConfirmButton() {
    return this.locator.locator("[data-testid^=\"comment-delete-confirm-\"]")
  }

  async openDeleteConfirmation() {
    const deleteButton = this.getDeleteButton()
    await expect(deleteButton).toBeVisible()
    await deleteButton.click()
    await expect(this.getDeleteConfirmButton()).toBeVisible()
  }

  async confirmDelete() {
    const confirmButton = this.getDeleteConfirmButton()
    await expect(confirmButton).toBeVisible()
    await confirmButton.click()
    await expect(this.getDeletedPlaceholder()).toBeVisible()
  }

  async delete() {
    await this.openDeleteConfirmation()
    await this.confirmDelete()
  }
}
