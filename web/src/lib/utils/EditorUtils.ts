export interface EditorContent {
  type: string
  text?: string
  content?: EditorContent[]
}

export class EditorUtils {
  static hasMeaningfulText(json?: EditorContent): boolean {
    if (!json) return false

    if (json.type === "text" && json.text?.trim()) {
      return true
    }

    if (json.content && Array.isArray(json.content)) {
      return json.content.some((c) => this.hasMeaningfulText(c))
    }

    return false
  }
}
