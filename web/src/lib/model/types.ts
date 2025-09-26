import type { EditorContent } from "$lib/utils/EditorUtils"

export interface CommunityPreview {
  id: string
  name: string
}

export interface PostPreview {
  id: string
  createdAt: string
  subject: string
  body: string
  contributor: ContributorRef
  community: CommunityRef
  commentsCount: number
}

export interface PostView {
  id: string
  createdAt: string
  subject: string
  body: string
  contributor: ContributorRef
  community: CommunityRef
  commentsCount: number
}

export interface CommunityPreviewPost {
  id: string
  createdAt: string
  subject: string
  body: string
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
}

export interface CommentView {
  id: string
  createdAt: string
  edited: boolean
  deleted: boolean
  deletedAt?: string | null
  contributor: ContributorRef
  html: string
  body?: EditorContent | null
}

export interface CommentPage {
  items: CommentView[]
  page: number
  pageSize: number
  total: number
}
