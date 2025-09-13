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
import org.jooq.DSLContext
import java.time.Instant
import java.time.ZoneOffset

@Path("/api/web/home")
class WebHomeResource(private val dslContext: DSLContext) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getWebHomePageData(@Context securityIdentity: SecurityIdentity): WebHomePageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        val selected = dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY,
            POST.contributor().HANDLE,
            POST.community().NAME,
            POST.community().SLUG
        )
            .from(POST)
            .fetch {
                val instant = it.get(POST.CREATED_AT).toInstant(ZoneOffset.UTC)
                PostPreview(
                    it.get(POST.ID).toString(),
                    instant,
                    it.get(POST.SUBJECT),
                    it.get(POST.BODY),
                    it.get(CONTRIBUTOR.HANDLE),
                    it.get(COMMUNITY.NAME),
                    it.get(COMMUNITY.SLUG)
                )
            }
        return WebHomePageData(selected)
    }

    data class PostPreview(
        val id: String, val createdAt: Instant, val subject: String, val body: String,
        val contributorHandle: String, val communityName: String, val communitySlug: String
    )

    data class WebHomePageData(val posts: List<PostPreview>)

}
