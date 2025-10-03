export const randomItem = <T>(array: T[]): T => {
  return array[Math.floor(Math.random() * array.length)]
}

export const randomEmoji = () => {
  const min = 0x1f600
  const max = 0x1f64f
  const codePoint = Math.floor(Math.random() * (max - min + 1)) + min
  return String.fromCodePoint(codePoint)
}

export const bgColors = [
  "#1E3A8A", // deep blue
  "#2563EB", // medium blue
  "#38BDF8", // sky blue
  "#0369A1", // ocean
  "#9333EA", // violet
  "#7C3AED", // purple
  "#A855F7", // lavender
  "#C084FC", // soft purple
  "#14B8A6", // teal
  "#22C55E", // spring green
  "#84CC16", // lime
  "#65A30D", // olive green
  "#F97316", // warm orange
  "#EA580C", // deep orange
  "#DC2626", // red
  "#EF4444", // bright red
  "#F87171", // coral red
  "#64748B", // slate grey
  "#475569", // deep slate
]
