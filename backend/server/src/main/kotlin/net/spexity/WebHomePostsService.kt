package net.spexity

import com.google.protobuf.Timestamp
import io.quarkus.grpc.GrpcService
import io.smallrye.mutiny.Uni
import net.spexity.data.model.public_.tables.Post.POST
import net.spexity.proto.GetPostsReply
import net.spexity.proto.GetPostsRequest
import net.spexity.proto.Post
import net.spexity.proto.WebHomePosts
import org.jboss.logging.Logger
import org.jooq.DSLContext
import java.time.ZoneOffset

@GrpcService
class WebHomePostsService(private val dslContext: DSLContext) : WebHomePosts {

    private val log = Logger.getLogger(WebHomePostsService::class.java)

    override fun getPosts(request: GetPostsRequest?): Uni<GetPostsReply?>? {
        log.info("Got request for getting posts")
        return Uni.createFrom().item {
            val selected = dslContext.selectFrom(POST).fetch()
            val posts = selected.map {
                val instant = it.createdAt.toInstant(ZoneOffset.UTC)
                Post.newBuilder()
                    .setId(it.id.toString())
                    .setCreatedAt(
                        Timestamp.newBuilder()
                            .setSeconds(instant.epochSecond)
                            .setNanos(instant.nano)
                            .build()
                    )
                    .setSubject(it.subject)
                    .setBody(it.body)
                    .build()
            }
            GetPostsReply.newBuilder().addAllPosts(posts).build()
        }
    }

}