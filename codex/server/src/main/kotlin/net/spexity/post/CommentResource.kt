package net.spexity.post

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import net.spexity.security.authCorrelationId
import java.util.UUID

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@Validated
class CommentResource(private val commentService: CommentService) {

    @GetMapping
    fun listComments(
        @PathVariable postId: UUID,
        @RequestParam(required = false) page: Int?,
        @RequestParam(required = false) pageSize: Int?,
        @RequestParam(required = false) order: String?,
    ): CommentService.ListResponse {
        val resolvedPage = page ?: 1
        val resolvedPageSize = pageSize ?: 100
        val resolvedOrder = when (order?.lowercase()) {
            "desc" -> "desc"
            else -> "asc"
        }
        return commentService.list(
            CommentService.ListRequest(
                postId = postId,
                page = resolvedPage,
                pageSize = resolvedPageSize,
                order = resolvedOrder
            )
        )
    }

    @PostMapping
    fun createComment(
        @PathVariable postId: UUID,
        @RequestBody request: CreateCommentRequest,
        authentication: Authentication
    ): CommentService.CreateResponse {
        return commentService.create(
            CommentService.CreateRequest(
                authCorrelationId(authentication),
                postId,
                request.bodyDocument
            )
        )
    }

    @PatchMapping("/{commentId}")
    fun editComment(
        @PathVariable postId: UUID,
        @PathVariable commentId: UUID,
        @RequestBody request: EditCommentRequest,
        authentication: Authentication
    ): ResponseEntity<Void> {
        commentService.edit(
            CommentService.EditRequest(
                authCorrelationId(authentication),
                postId,
                commentId,
                request.bodyDocument
            )
        )
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @PathVariable postId: UUID,
        @PathVariable commentId: UUID,
        authentication: Authentication
    ): ResponseEntity<Void> {
        commentService.softDelete(
            CommentService.DeleteRequest(
                authCorrelationId(authentication),
                postId,
                commentId
            )
        )
        return ResponseEntity.noContent().build()
    }

    data class CreateCommentRequest(val bodyDocument: Document)
    data class EditCommentRequest(val bodyDocument: Document)
}
