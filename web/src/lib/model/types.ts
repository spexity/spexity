export interface CommunityPreview {
  id: string
  name: string
  postsCount: number
}

export interface PostPreview {
  id: string
  createdAt: string
  subject: string
  bodyText: string
  contributor: ContributorRef
  community: CommunityRef
  commentsCount: number
}

export interface PostView {
  id: string
  createdAt: string
  subject: string
  bodyHtml: string
  contributor: ContributorRef
  community: CommunityRef
  commentsCount: number
}

export interface CommunityPreviewPost {
  id: string
  createdAt: string
  subject: string
  bodyText: string
  contributor: ContributorRef
  commentsCount: number
}

export interface CommunityRef {
  id: string
  name: string
}

export interface ContributorRef {
  id: string
  handle: string
  avatarEmojis: string
  avatarBgColor: string
}

export interface CommentView {
  id: string
  createdAt: string
  editCount: number | null
  deleted: boolean
  contributor: ContributorRef
  bodyHtml: string | null
}

export interface CommentPage {
  items: CommentView[]
  page: number
  pageSize: number
}

export type OrderPref = "asc" | "desc"

export interface Prefs {
  timezone: string
  locale: string
  commentsOrder: OrderPref
}
