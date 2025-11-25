package net.spexity.post

import net.spexity.data.model.public_.Tables.POST_COMMENT
import net.spexity.data.model.public_.Tables.POST_COMMENT_REVISION
import net.spexity.security.SecurityService
import net.spexity.web.model.ContributorRef
import org.jooq.DSLContext
import org.jooq.JSONB
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.ObjectMapper
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@Service
class CommentService(
    private val dslContext: DSLContext,
    private val securityService: SecurityService,
    private val objectMapper: ObjectMapper,
    @Value("\${spexity.rate-limit.excluded-auth-correlation-ids:}") excludedAuthCorrelationIds: List<String>
) {
    private val excludedAuthCorrelationIds = excludedAuthCorrelationIds.toSet()

    fun list(request: ListRequest): ListResponse {
        val page = request.page.coerceAtLeast(1)
        val size = request.pageSize.coerceIn(1, MAX_PAGE_SIZE)
        val offset = (page - 1) * size

        val rows = dslContext.select(
            POST_COMMENT.ID,
            POST_COMMENT.CREATED_AT,
            POST_COMMENT.EDIT_COUNT,
            POST_COMMENT.DELETED_AT,
            POST_COMMENT.BODY_JSON,
            POST_COMMENT.contributor().ID,
            POST_COMMENT.contributor().HANDLE,
            POST_COMMENT.contributor().AVATAR_TEXT,
            POST_COMMENT.contributor().AVATAR_BG_COLOR
        )
            .from(POST_COMMENT)
            .where(POST_COMMENT.POST_ID.eq(request.postId))
            .orderBy(
                if (request.order == "desc") POST_COMMENT.CREATED_AT.desc() else POST_COMMENT.CREATED_AT.asc(),
                if (request.order == "desc") POST_COMMENT.ID.desc() else POST_COMMENT.ID.asc(),
            )
            .limit(size)
            .offset(offset)
            .fetch()

        val commentViews = rows.map {
            val createdAt = it.get(POST_COMMENT.CREATED_AT).toInstant()
            val contributor = ContributorRef(
                it.get(POST_COMMENT.contributor().ID),
                it.get(POST_COMMENT.contributor().HANDLE),
                it.get(POST_COMMENT.contributor().AVATAR_TEXT),
                it.get(POST_COMMENT.contributor().AVATAR_BG_COLOR)
            )
            val editCount = it.get(POST_COMMENT.EDIT_COUNT)
            val deletedAt = it.get(POST_COMMENT.DELETED_AT)
            val deleted = deletedAt != null
            val bodyHtml = if (!deleted) {
                val document = objectMapper.readValue(it.get(POST_COMMENT.BODY_JSON).data(), Document::class.java)
                HtmlSanitizer.sanitize(DocumentToHtmlSerializer.serialize(document))
            } else {
                null
            }
            CommentView(
                id = it.get(POST_COMMENT.ID),
                createdAt = createdAt,
                editCount = editCount,
                deleted = deleted,
                contributor = contributor,
                bodyHtml = bodyHtml
            )
        }

        return ListResponse(commentViews, page, size)
    }

    fun create(request: CreateRequest): CreateResponse {
        val contributorId =
            securityService.validateVerifiedGetContributorId(request.authCorrelationId)
        enforceRateLimit(contributorId, request.authCorrelationId)
        validateBodyDocument(request.bodyDocument)
        val commentRecord = dslContext.newRecord(POST_COMMENT)
        commentRecord.postId = request.postId
        commentRecord.contributorId = contributorId
        commentRecord.bodyJson = JSONB.jsonb(objectMapper.writeValueAsString(request.bodyDocument))
        commentRecord.store()
        return CreateResponse(commentRecord.id)
    }

    fun edit(request: EditRequest) {
        val contributorId =
            securityService.validateVerifiedGetContributorId(request.authCorrelationId)
        validateBodyDocument(request.bodyDocument)
        return dslContext.transactionResult { transactionManager ->
            val commentRecord = transactionManager.dsl().selectFrom(POST_COMMENT)
                .where(POST_COMMENT.ID.eq(request.commentId))
                .and(POST_COMMENT.POST_ID.eq(request.postId))
                .forUpdate()
                .fetchOne() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found")
            if (commentRecord.contributorId != contributorId) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit the comment")
            }
            if (commentRecord.deletedAt != null) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Comment already deleted")
            }
            val currentEditCount = commentRecord.editCount ?: 0
            if (currentEditCount >= MAX_EDITS) {
                throw ResponseStatusException(HttpStatus.CONFLICT, "Comment edit limit reached")
            }

            val revisionRecord = transactionManager.dsl().newRecord(POST_COMMENT_REVISION)
            revisionRecord.commentId = commentRecord.id
            revisionRecord.authoredAt = commentRecord.editedAt ?: commentRecord.createdAt
            revisionRecord.bodyJson = commentRecord.bodyJson
            revisionRecord.store()

            commentRecord.bodyJson = JSONB.valueOf(objectMapper.writeValueAsString(request.bodyDocument))
            commentRecord.editedAt = OffsetDateTime.now(ZoneOffset.UTC)
            commentRecord.editCount = currentEditCount + 1
            commentRecord.store()
        }
    }

    fun softDelete(request: DeleteRequest) {
        val contributorId =
            securityService.validateVerifiedGetContributorId(request.authCorrelationId)

        val commentRecord = dslContext.selectFrom(POST_COMMENT)
            .where(POST_COMMENT.ID.eq(request.commentId))
            .and(POST_COMMENT.POST_ID.eq(request.postId))
            .forUpdate()
            .fetchOne() ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found")

        if (commentRecord.contributorId != contributorId) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete the comment")
        }
        if (commentRecord.deletedAt != null) {
            return
        }
        commentRecord.deletedAt = OffsetDateTime.now(ZoneOffset.UTC)
        commentRecord.store()
    }

    private fun validateBodyDocument(document: Document) {
        val trimmedText = DocumentToTextSerializer.serialize(document).trim()
        if (trimmedText.isBlank()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment must contain meaningful text")
        }
        if (trimmedText.length > MAX_LENGTH) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment exceeds maximum length")
        }
    }

    private fun enforceRateLimit(contributorId: UUID, authCorrelationId: String) {
        if (excludedAuthCorrelationIds.contains(authCorrelationId)) {
            return
        }
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val burstWindow = now.minusSeconds(BURST_WINDOW_SECONDS)
        val recentCount = dslContext.selectCount()
            .from(POST_COMMENT)
            .where(POST_COMMENT.CONTRIBUTOR_ID.eq(contributorId))
            .and(POST_COMMENT.CREATED_AT.ge(burstWindow))
            .fetchOne(0, Int::class.java) ?: 0
        if (recentCount >= BURST_CAPACITY) {
            throw ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many requests")
        }
    }

    data class ListRequest(val postId: UUID, val page: Int, val pageSize: Int, val order: String = "asc")
    data class ListResponse(val items: List<CommentView>, val page: Int, val pageSize: Int)
    data class CreateRequest(val authCorrelationId: String, val postId: UUID, val bodyDocument: Document)
    data class CreateResponse(val id: UUID)
    data class EditRequest(
        val authCorrelationId: String,
        val postId: UUID,
        val commentId: UUID,
        val bodyDocument: Document
    )
    data class DeleteRequest(val authCorrelationId: String, val postId: UUID, val commentId: UUID)

    data class CommentView(
        val id: UUID,
        val createdAt: Instant,
        val editCount: Int?,
        val deleted: Boolean,
        val contributor: ContributorRef,
        val bodyHtml: String?,
    )

    companion object {
        private const val MAX_LENGTH = 2000
        private const val MAX_EDITS = 2
        private const val BURST_CAPACITY = 1
        private const val BURST_WINDOW_SECONDS = 30L
        private const val MAX_PAGE_SIZE = 100
    }
}
