package net.spexity.web

import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.*
import net.spexity.web.model.CommunityRef
import net.spexity.web.model.ContributorRef
import net.spexity.web.model.PostView
import org.jooq.DSLContext
import java.time.ZoneOffset
import java.util.*

@Path("/api/web/posts")
class WebPostsResource(private val dslContext: DSLContext) {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getPostPageData(@PathParam("id") id: UUID): PostPageData {
        val selected = dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY,
            POST.contributor().ID,
            POST.contributor().HANDLE,
            POST.community().ID,
            POST.community().NAME
        )
            .from(POST)
            .where(POST.ID.eq(id))
            .fetchOne {
                val instant = it.get(POST.CREATED_AT).toInstant(ZoneOffset.UTC)
                PostView(
                    it.get(POST.ID),
                    instant,
                    it.get(POST.SUBJECT),
                    it.get(POST.BODY),
                    ContributorRef(it.get(CONTRIBUTOR.ID), it.get(CONTRIBUTOR.HANDLE)),
                    CommunityRef(it.get(COMMUNITY.ID), it.get(COMMUNITY.NAME))
                )
            }
        if (selected == null) {
            throw NotFoundException("Post not found")
        }
        return PostPageData(selected)
    }

    data class PostPageData(val post: PostView)

}
