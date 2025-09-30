package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.Tables.*
import net.spexity.security.optionalAuthCorrelationId
import net.spexity.web.model.CommunityRef
import net.spexity.web.model.ContributorRef
import net.spexity.web.model.PostPreview
import org.jooq.DSLContext

@Path("/api/web/home")
class WebHomeResource(private val dslContext: DSLContext) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getHomePageData(@Context securityIdentity: SecurityIdentity): HomePageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        val selected = dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY_TEXT,
            POST.contributor().ID,
            POST.contributor().HANDLE,
            POST.contributor().AVATAR_EMOJI,
            POST.contributor().AVATAR_BG_COLOR,
            POST.community().ID,
            POST.community().NAME,
            POST.COMMENTS_COUNT,
        )
            .from(POST)
            .fetch {
                val instant = it.get(POST.CREATED_AT).toInstant()
                PostPreview(
                    it.get(POST.ID),
                    instant,
                    it.get(POST.SUBJECT),
                    it.get(POST.BODY_TEXT).take(512),
                    ContributorRef(
                        it.get(POST.contributor().ID),
                        it.get(POST.contributor().HANDLE),
                        it.get(POST.contributor().AVATAR_EMOJI),
                        it.get(POST.contributor().AVATAR_BG_COLOR),
                    ),
                    CommunityRef(it.get(COMMUNITY.ID), it.get(COMMUNITY.NAME)),
                    it.get(POST.COMMENTS_COUNT)
                )
            }
        return HomePageData(selected)
    }

    data class HomePageData(val posts: List<PostPreview>)

}
