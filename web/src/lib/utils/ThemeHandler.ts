import { APP_THEME } from "../../app-theme"

export class ThemeHandler {
  static handle() {
    let currentlyDark =
      window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches
    document.documentElement.setAttribute(
      "data-theme",
      currentlyDark ? APP_THEME.dark : APP_THEME.light,
    )

    if (window.matchMedia) {
      let listener = (e: MediaQueryListEvent) => {
        document.documentElement.setAttribute(
          "data-theme",
          e.matches ? APP_THEME.dark : APP_THEME.light,
        )
      }
      window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", listener)
      return () => {
        window.matchMedia("(prefers-color-scheme: dark)").removeEventListener("change", listener)
      }
    }
    return () => {}
  }
}
