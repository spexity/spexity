package net.spexity.post

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import net.spexity.security.authCorrelationId
import java.util.*

@Path("/api/posts/{postId}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CommentResource(private val commentService: CommentService) {

    @GET
    @PermitAll
    fun listComments(
        @PathParam("postId") postId: UUID,
        @QueryParam("page") page: Int?,
        @QueryParam("pageSize") pageSize: Int?,
    ): CommentService.ListResponse {
        val resolvedPage = page ?: 1
        val resolvedPageSize = pageSize ?: 100
        return commentService.list(
            CommentService.ListRequest(
                postId = postId,
                page = resolvedPage,
                pageSize = resolvedPageSize
            )
        )
    }

    @POST
    @Authenticated
    fun createComment(
        @PathParam("postId") postId: UUID,
        request: CreateCommentRequest,
        @Context securityIdentity: SecurityIdentity
    ): CommentService.CreateResponse {
        return commentService.create(
            CommentService.CreateRequest(
                authCorrelationId(securityIdentity),
                postId,
                request.bodyDocument
            )
        )
    }

    @PATCH
    @Path("/{commentId}")
    @Authenticated
    fun editComment(
        @PathParam("postId") postId: UUID,
        @PathParam("commentId") commentId: UUID,
        request: EditCommentRequest,
        @Context securityIdentity: SecurityIdentity
    ): Response {
        commentService.edit(
            CommentService.EditRequest(
                authCorrelationId(securityIdentity),
                postId,
                commentId,
                request.bodyDocument
            )
        )
        return Response.ok().build()
    }

    @DELETE
    @Path("/{commentId}")
    @Authenticated
    fun deleteComment(
        @PathParam("postId") postId: UUID,
        @PathParam("commentId") commentId: UUID,
        @Context securityIdentity: SecurityIdentity
    ) {
        commentService.softDelete(
            CommentService.DeleteRequest(
                authCorrelationId(securityIdentity),
                postId,
                commentId
            )
        )
    }

    data class CreateCommentRequest(val bodyDocument: Document)
    data class EditCommentRequest(val bodyDocument: Document)
}
