package net.spexity.web

import net.spexity.data.model.public_.Tables.COMMUNITY
import net.spexity.data.model.public_.Tables.POST
import net.spexity.post.CommentService
import net.spexity.post.Document
import net.spexity.post.DocumentToHtmlSerializer
import net.spexity.post.HtmlSanitizer
import net.spexity.web.model.CommunityRef
import net.spexity.web.model.ContributorRef
import net.spexity.web.model.PostView
import org.jooq.DSLContext
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.ObjectMapper
import java.util.UUID

@RestController
@RequestMapping("/api/web/posts")
class WebPostsResource(
    private val dslContext: DSLContext,
    private val objectMapper: ObjectMapper,
    private val commentService: CommentService
) {

    @GetMapping("/{id}")
    fun getPostPageData(
        @PathVariable id: UUID,
        @RequestParam(required = false) order: String?
    ): PostPageData {
        val selected = dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY_JSON,
            POST.COMMENTS_COUNT,
            POST.contributor().ID,
            POST.contributor().HANDLE,
            POST.contributor().AVATAR_TEXT,
            POST.contributor().AVATAR_BG_COLOR,
            POST.community().ID,
            POST.community().NAME,
        ).from(POST).where(POST.ID.eq(id)).fetchOne {
            val instant = it.get(POST.CREATED_AT).toInstant()
            val bodyDocument: Document = objectMapper.readValue(it.get(POST.BODY_JSON).data(), Document::class.java)
            PostView(
                it.get(POST.ID),
                instant,
                it.get(POST.SUBJECT),
                HtmlSanitizer.sanitize(DocumentToHtmlSerializer.serialize(bodyDocument)),
                ContributorRef(
                    it.get(POST.contributor().ID),
                    it.get(POST.contributor().HANDLE),
                    it.get(POST.contributor().AVATAR_TEXT),
                    it.get(POST.contributor().AVATAR_BG_COLOR),
                ),
                CommunityRef(it.get(COMMUNITY.ID), it.get(COMMUNITY.NAME)),
                it.get(POST.COMMENTS_COUNT)
            )
        } ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found")

        val comments = commentService.list(
            CommentService.ListRequest(
                postId = id,
                page = 1,
                pageSize = 100,
                order = order ?: "asc"
            )
        )
        return PostPageData(selected, comments)
    }

    @GetMapping("/new")
    fun getNewPostData(@RequestParam("communityId") id: UUID): NewPostPageData {
        val selected = dslContext.select(
            COMMUNITY.NAME,
        ).from(COMMUNITY).where(COMMUNITY.ID.eq(id)).fetchOne(COMMUNITY.NAME)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found")
        return NewPostPageData(selected)
    }

    data class PostPageData(val post: PostView, val comments: CommentService.ListResponse)

    data class NewPostPageData(val communityName: String)
}
