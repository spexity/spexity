interface LocaleDesc {
  id: string
  label: string
  rtl?: boolean
}

export const LOCALES: LocaleDesc[] = [
  {
    id: "en",
    label: "English",
  },
  {
    id: "ar",
    label: "العربية",
    rtl: true,
  },
  {
    id: "zh-cn",
    label: "简体中文",
  },
]

export const LOCALES_MAP = LOCALES.reduce(
  (map, locale) => {
    map[locale.id] = locale
    return map
  },
  {} as Record<string, LocaleDesc>,
)
