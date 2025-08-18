package net.spexity

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.tables.Post.POST
import org.jooq.DSLContext
import java.time.Instant
import java.time.ZoneOffset


@Path("/web-api/home-page")
class WebHomePageResource(private val dslContext: DSLContext) {

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    fun getWebHomePageData(): WebHomePageData {
        val selected = dslContext.selectFrom(POST).fetch()
        val posts = selected.map {
            val instant = it.createdAt.toInstant(ZoneOffset.UTC)
            PostDto(it.id.toString(), instant, it.subject, it.body)
        }
        return WebHomePageData(posts)
    }

    data class PostDto(val id: String, val createdIt: Instant, val subject: String, val body: String)

    data class WebHomePageData(val posts: List<PostDto>)

}