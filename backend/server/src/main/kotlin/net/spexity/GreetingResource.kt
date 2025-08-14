package net.spexity

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import net.spexity.data.model.public_.tables.Post.POST
import org.jooq.DSLContext


@Path("/hello")
class GreetingResource(private val dslContext: DSLContext) {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello(): String {
        val selected = dslContext.selectFrom(POST).fetch()
        val subjects = selected.map { it.subject }
        return "Hello from Quarkus REST\n" + subjects.joinToString(", ")
    }

}