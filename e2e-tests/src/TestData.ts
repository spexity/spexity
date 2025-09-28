export interface UserCredentials {
  username: string,
  password: string
}

export const POST_WITH_COMMENTS = "8032dd4e-1abd-434d-b92c-7c39f8ca359d"
export const POST_WITHOUT_COMMENTS = "1032dd4e-1abd-434d-b92c-7c39f8ca359d"
export const GOD_USER: UserCredentials = { username: "god@example.com", password: "god" }
export const VERIFIED_USER: UserCredentials = { username: "human@example.com", password: "human" }
export const UNVERIFIED_USER: UserCredentials = { username: "bot@example.com", password: "bot" }
