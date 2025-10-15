export type Theme = "light" | "system" | "dark"

export class ThemeHandler {
  static handle(theme: Theme) {
    if (theme === "system") {
      document.documentElement.removeAttribute("data-theme")
    } else {
      document.documentElement.setAttribute("data-theme", theme)
    }
  }
}
