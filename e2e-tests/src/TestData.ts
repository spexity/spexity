export interface UserCredentials {
  username: string,
  password: string
}

export const POST_WITH_LOTS_COMMENTS = "17e1af07-467d-4cce-91fa-50e784064b2c"
export const POST_WITH_NO_COMMENTS_EVER = "87471c08-ae55-443c-896c-ebbcab50453c"
export const POST_WITH_NO_COMMENTS_YET = "85200022-1089-44c6-8a08-610ce321fa9f"
export const GOD_USER: UserCredentials = { username: "god@example.com", password: "god" }
export const VERIFIED_USER: UserCredentials = { username: "human@example.com", password: "human" }
export const UNVERIFIED_USER: UserCredentials = { username: "bot@example.com", password: "bot" }
