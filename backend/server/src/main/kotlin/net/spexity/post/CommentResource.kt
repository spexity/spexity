package net.spexity.post

import io.quarkus.security.Authenticated
import io.quarkus.security.identity.SecurityIdentity
import jakarta.annotation.security.PermitAll
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import net.spexity.security.authCorrelationId
import net.spexity.security.optionalAuthCorrelationId
import java.util.UUID

@Path("/api/posts/{postId}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class CommentResource(private val commentService: CommentService) {

    @POST
    @Authenticated
    fun createComment(
        @PathParam("postId") postId: UUID,
        @Valid request: CreateCommentRequest,
        @Context securityIdentity: SecurityIdentity
    ): CommentService.CommentView {
        return commentService.create(
            CommentService.CreateRequest(
                authCorrelationId(securityIdentity),
                postId,
                request.body
            )
        )
    }

    @GET
    @PermitAll
    fun listComments(
        @PathParam("postId") postId: UUID,
        @QueryParam("page") page: Int?,
        @QueryParam("pageSize") pageSize: Int?,
        @Context securityIdentity: SecurityIdentity
    ): CommentService.CommentPage {
        val resolvedPage = page ?: 1
        val resolvedPageSize = pageSize ?: 100
        return commentService.list(
            CommentService.ListRequest(
                postId = postId,
                page = resolvedPage,
                pageSize = resolvedPageSize,
                authCorrelationId = optionalAuthCorrelationId(securityIdentity)
            )
        )
    }

    @PATCH
    @Path("/{commentId}")
    @Authenticated
    fun editComment(
        @PathParam("postId") postId: UUID,
        @PathParam("commentId") commentId: UUID,
        @Valid request: EditCommentRequest,
        @Context securityIdentity: SecurityIdentity
    ): CommentService.CommentView {
        return commentService.edit(
            CommentService.EditRequest(
                authCorrelationId(securityIdentity),
                postId,
                commentId,
                request.body
            )
        )
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

    data class CreateCommentRequest(val body: Doc)
    data class EditCommentRequest(val body: Doc)
}
