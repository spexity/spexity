package net.spexity.post

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.ForbiddenException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status
import net.spexity.data.model.public_.Tables.POST_COMMENT
import net.spexity.data.model.public_.Tables.POST_COMMENT_REVISION
import net.spexity.security.SecurityService
import net.spexity.web.model.ContributorRef
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jooq.DSLContext
import org.jooq.JSONB
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@ApplicationScoped
class CommentService(
    private val dslContext: DSLContext,
    private val securityService: SecurityService,
    private val objectMapper: ObjectMapper,
    @param:ConfigProperty(name = "spexity.rate-limit.excluded-auth-correlation-ids")
    private val excludedAuthCorrelationIds: Set<String>
) {

    fun list(request: ListRequest): ListResponse {
        val page = request.page.coerceAtLeast(1)
        val size = request.pageSize.coerceIn(1, MAX_PAGE_SIZE)
        val offset = (page - 1) * size

//        TODO: this pagination should be changed to use filtering pagination (createdAt > lastMaxCreatedAt)
        val rows = dslContext.select(
            POST_COMMENT.ID,
            POST_COMMENT.CREATED_AT,
            POST_COMMENT.EDIT_COUNT,
            POST_COMMENT.DELETED_AT,
            POST_COMMENT.BODY_JSON,
            POST_COMMENT.contributor().ID,
            POST_COMMENT.contributor().HANDLE
        )
            .from(POST_COMMENT)
            .where(POST_COMMENT.POST_ID.eq(request.postId))
            .orderBy(POST_COMMENT.CREATED_AT.asc(), POST_COMMENT.ID.asc())
            .limit(size)
            .offset(offset)
            .fetch()

        val commentViews = rows.map { record ->
            val createdAt = record.get(POST_COMMENT.CREATED_AT).toInstant()
            val contributor = ContributorRef(
                record.get(POST_COMMENT.contributor().ID),
                record.get(POST_COMMENT.contributor().HANDLE)
            )
            val editCount = record.get(POST_COMMENT.EDIT_COUNT)
            val deletedAt = record.get(POST_COMMENT.DELETED_AT)
            val deleted = deletedAt != null
            val bodyHtml = if (!deleted) {
                val document = objectMapper.readValue(record.get(POST_COMMENT.BODY_JSON).data(), Document::class.java)
                HtmlSanitizer.sanitize(DocumentToHtmlSerializer.serialize(document))
            } else {
                null
            }
            CommentView(
                id = record.get(POST_COMMENT.ID),
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
                .fetchOne() ?: throw NotFoundException("Comment not found")
//          TODO: should we add some sort of LevenshteinDistance so people don't go around completely changing their comment.
            if (commentRecord.contributorId != contributorId) {
                throw ForbiddenException("Only the author can edit the comment")
            }
            if (commentRecord.deletedAt != null) {
                throw ClientErrorException("Comment already deleted", Response.Status.CONFLICT)
            }
            val currentEditCount = commentRecord.editCount ?: 0
            if (currentEditCount >= MAX_EDITS) {
                throw ClientErrorException("Comment edit limit reached", Response.Status.CONFLICT)
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
            .fetchOne() ?: throw NotFoundException("Comment not found")

        if (commentRecord.contributorId != contributorId) {
            throw ForbiddenException("Only the author can delete the comment")
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
            throw BadRequestException("Comment must contain meaningful text")
        }
        if (trimmedText.length > MAX_LENGTH) {
            throw BadRequestException("Comment exceeds maximum length")
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
            throw ClientErrorException(
                Response.status(Status.TOO_MANY_REQUESTS)
                    .header("Retry-After", RETRY_AFTER_SECONDS)
                    .build()
            )
        }
    }

    data class ListRequest(val postId: UUID, val page: Int, val pageSize: Int)
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
        private const val RETRY_AFTER_SECONDS = 30
        private const val MAX_PAGE_SIZE = 100
    }
}
