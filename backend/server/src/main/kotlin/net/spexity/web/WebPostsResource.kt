package net.spexity.web

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Context
import io.quarkus.security.identity.SecurityIdentity
import net.spexity.data.model.public_.Tables.*
import net.spexity.post.CommentService
import net.spexity.post.BodyHtmlSerializer
import net.spexity.post.Doc
import net.spexity.post.HtmlSanitizer
import net.spexity.security.optionalAuthCorrelationId
import net.spexity.web.model.CommunityRef
import net.spexity.web.model.ContributorRef
import net.spexity.web.model.PostView
import org.jooq.DSLContext
import java.time.ZoneOffset
import java.util.*

@Path("/api/web/posts")
class WebPostsResource(
    private val dslContext: DSLContext,
    private val objectMapper: ObjectMapper,
    private val commentService: CommentService
) {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getPostPageData(
        @PathParam("id") id: UUID,
        @Context securityIdentity: SecurityIdentity
    ): PostPageData {
        val selected = dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY_JSON,
            POST.contributor().ID,
            POST.contributor().HANDLE,
            POST.community().ID,
            POST.community().NAME,
            POST.COMMENTS_COUNT
        ).from(POST).where(POST.ID.eq(id)).fetchOne {
            val instant = it.get(POST.CREATED_AT).toInstant(ZoneOffset.UTC)
            val body: Doc = objectMapper.readValue(it.get(POST.BODY_JSON).data(), Doc::class.java)
            PostView(
                it.get(POST.ID),
                instant,
                it.get(POST.SUBJECT),
                HtmlSanitizer.sanitize(BodyHtmlSerializer.render(body)),
                ContributorRef(it.get(CONTRIBUTOR.ID), it.get(CONTRIBUTOR.HANDLE)),
                CommunityRef(it.get(COMMUNITY.ID), it.get(COMMUNITY.NAME)),
                it.get(POST.COMMENTS_COUNT)
            )
        }
        if (selected == null) {
            throw NotFoundException("Post not found")
        }
        val comments = commentService.list(
            CommentService.ListRequest(
                postId = id,
                page = 1,
                pageSize = 100,
                authCorrelationId = optionalAuthCorrelationId(securityIdentity)
            )
        )
        return PostPageData(selected, comments)
    }

    @GET
    @Path("/new")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getNewPostData(@QueryParam("communityId") id: UUID): NewPostPageData {
        val selected = dslContext.select(
            COMMUNITY.NAME,
        ).from(COMMUNITY).where(COMMUNITY.ID.eq(id)).fetchOne(COMMUNITY.NAME)
        if (selected == null) {
            throw NotFoundException("Community not found")
        }
        return NewPostPageData(selected)
    }

    data class PostPageData(val post: PostView, val comments: CommentService.CommentPage)

    data class NewPostPageData(val communityName: String)

}
