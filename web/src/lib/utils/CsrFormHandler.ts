export class CsrFormHandler {
  static onsubmit(event: SubmitEvent): FormData {
    event.preventDefault()
    const form = event.currentTarget as HTMLFormElement
    return new FormData(form)
  }
}
