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
}

export interface PostView {
  id: string
  createdAt: string
  subject: string
  body: string
  contributor: ContributorRef
  community: CommunityRef
}

export interface CommunityPreviewPost {
  id: string
  createdAt: string
  subject: string
  body: string
  contributor: ContributorRef
}

export interface CommunityRef {
  id: string
  name: string
}

export interface ContributorRef {
  id: string
  handle: string
}
