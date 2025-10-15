package net.spexity.web.model

import java.time.Instant
import java.util.*

data class CommunityPreview(
    val id: UUID, val name: String,
    val postsCount: Int, val isMember: Boolean
)

data class PostPreview(
    val id: UUID, val createdAt: Instant, val subject: String, val bodyText: String,
    val contributor: ContributorRef, val community: CommunityRef, val commentsCount: Int
)

data class PostView(
    val id: UUID, val createdAt: Instant, val subject: String, val bodyHtml: String,
    val contributor: ContributorRef, val community: CommunityRef, val commentsCount: Int
)


data class CommunityPreviewPost(
    val id: UUID, val createdAt: Instant, val subject: String, val bodyText: String,
    val contributor: ContributorRef, val commentsCount: Int
)

data class CommunityRef(val id: UUID, val name: String)

data class ContributorRef(
    val id: UUID, val handle: String,
    val avatarText: String, val avatarBgColor: String
)
