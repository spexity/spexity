export interface UserCredentials {
  username: string,
  password: string
}

export const POST_WITH_COMMENTS = "17e1af07-467d-4cce-91fa-50e784064b2c"
export const POST_WITHOUT_COMMENTS = "87471c08-ae55-443c-896c-ebbcab50453c"
export const GOD_USER: UserCredentials = { username: "god@example.com", password: "god" }
export const VERIFIED_USER: UserCredentials = { username: "human@example.com", password: "human" }
export const UNVERIFIED_USER: UserCredentials = { username: "bot@example.com", password: "bot" }
