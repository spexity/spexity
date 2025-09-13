package net.spexity.web

import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.tables.Community
import net.spexity.data.model.public_.tables.Contributor
import net.spexity.data.model.public_.tables.Post
import net.spexity.security.optionalAuthCorrelationId
import org.jooq.DSLContext
import java.time.Instant
import java.time.ZoneOffset

@Path("/api/web/home")
class WebHomeResource(private val dslContext: DSLContext) {

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    fun getWebHomePageData(@Context securityIdentity: SecurityIdentity): WebHomePageData {
        val authCorrelationId = optionalAuthCorrelationId(securityIdentity)
        val selected = dslContext.select(
            Post.POST.ID,
            Post.POST.CREATED_AT,
            Post.POST.SUBJECT,
            Post.POST.BODY,
            Post.POST.contributor().HANDLE,
            Post.POST.community().NAME,
            Post.POST.community().SLUG
        )
            .from(Post.POST)
            .fetch {
                val instant = it.get(Post.POST.CREATED_AT).toInstant(ZoneOffset.UTC)
                PostPreview(
                    it.get(Post.POST.ID).toString(),
                    instant,
                    it.get(Post.POST.SUBJECT),
                    it.get(Post.POST.BODY),
                    it.get(Contributor.CONTRIBUTOR.HANDLE),
                    it.get(Community.COMMUNITY.NAME),
                    it.get(Community.COMMUNITY.SLUG)
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
