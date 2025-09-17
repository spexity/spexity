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
import java.time.ZoneOffset

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
            POST.community().ID,
            POST.community().NAME,
        )
            .from(POST)
            .fetch {
                val instant = it.get(POST.CREATED_AT).toInstant(ZoneOffset.UTC)
                PostPreview(
                    it.get(POST.ID),
                    instant,
                    it.get(POST.SUBJECT),
                    it.get(POST.BODY_TEXT).take(512),
                    ContributorRef(it.get(CONTRIBUTOR.ID), it.get(CONTRIBUTOR.HANDLE)),
                    CommunityRef(it.get(COMMUNITY.ID), it.get(COMMUNITY.NAME))
                )
            }
        return HomePageData(selected)
    }

    data class HomePageData(val posts: List<PostPreview>)

}
