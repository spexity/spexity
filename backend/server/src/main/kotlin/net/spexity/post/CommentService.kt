package net.spexity.post

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.ClientErrorException
import jakarta.ws.rs.ForbiddenException
import jakarta.ws.rs.NotFoundException
import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.POST
import net.spexity.data.model.public_.Tables.POST_COMMENT
import net.spexity.data.model.public_.Tables.POST_COMMENT_REVISION
import net.spexity.security.SecurityService
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

@ApplicationScoped
class CommentService(
    private val dsl: DSLContext,
    private val securityService: SecurityService,
    private val objectMapper: ObjectMapper
) {

    fun create(request: CreateRequest): CommentView {
        securityService.validateVerified(request.authCorrelationId)
        val author = fetchContributor(request.authCorrelationId)
            ?: throw ForbiddenException("User must be registered to comment")
        ensurePostExists(request.postId)
        enforceRateLimit(author.id)

        val bodyJson = sanitizeAndValidateBody(request.body)
        val commentRecord = dsl.newRecord(POST_COMMENT)
        commentRecord.postId = request.postId
        commentRecord.contributorId = author.id
        commentRecord.bodyJson = JSONB.valueOf(objectMapper.writeValueAsString(bodyJson.body))
        commentRecord.bodyText = bodyJson.bodyText
        commentRecord.store()
        val persisted = dsl.selectFrom(POST_COMMENT)
            .where(POST_COMMENT.ID.eq(commentRecord.id))
            .fetchOne() ?: throw WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR)

        return bodyJson.toView(
            persisted.id,
            persisted.createdAt?.toInstant() ?: Instant.now(),
            edited = false,
            deleted = false,
            deletedAt = null,
            contributor = CommentContributor(author.id, author.handle)
        )
    }

    fun list(request: ListRequest): CommentPage {
        val page = request.page.coerceAtLeast(1)
        val size = request.pageSize.coerceIn(1, MAX_PAGE_SIZE)
        val offset = (page - 1) * size

        val total = dsl.selectCount()
            .from(POST_COMMENT)
            .where(POST_COMMENT.POST_ID.eq(request.postId))
            .fetchOne(0, Int::class.java) ?: 0

        if (total == 0) {
            return CommentPage(emptyList(), page, size, total)
        }

        val currentContributor = request.authCorrelationId?.let { fetchContributor(it) }

        val rows = dsl.select(
            POST_COMMENT.ID,
            POST_COMMENT.CREATED_AT,
            POST_COMMENT.EDITED_AT,
            POST_COMMENT.EDIT_COUNT,
            POST_COMMENT.DELETED_AT,
            POST_COMMENT.DELETED_BY_AUTHOR,
            POST_COMMENT.BODY_JSON,
            POST_COMMENT.BODY_TEXT,
            CONTRIBUTOR.ID,
            CONTRIBUTOR.HANDLE
        )
            .from(POST_COMMENT)
            .join(CONTRIBUTOR).on(CONTRIBUTOR.ID.eq(POST_COMMENT.CONTRIBUTOR_ID))
            .where(POST_COMMENT.POST_ID.eq(request.postId))
            .orderBy(POST_COMMENT.CREATED_AT.asc(), POST_COMMENT.ID.asc())
            .limit(size)
            .offset(offset)
            .fetch()

        val commentViews = rows.map { record ->
            val contributor = CommentContributor(
                record.get(CONTRIBUTOR.ID),
                record.get(CONTRIBUTOR.HANDLE)
            )
            val deletedAt = record.get(POST_COMMENT.DELETED_AT)
            val deleted = deletedAt != null
            val docAndHtml = if (!deleted) {
                val doc = objectMapper.readValue(record.get(POST_COMMENT.BODY_JSON).data(), Doc::class.java)
                val html = HtmlSanitizer.sanitize(BodyHtmlSerializer.render(doc))
                val body = if (currentContributor?.id == contributor.id) doc else null
                DocHtml(body, html)
            } else {
                DocHtml(null, DELETED_PLACEHOLDER_HTML)
            }
            CommentView(
                id = record.get(POST_COMMENT.ID),
                createdAt = record.get(POST_COMMENT.CREATED_AT)?.toInstant() ?: Instant.now(),
                edited = record.get(POST_COMMENT.EDIT_COUNT) > 0,
                deleted = deleted,
                deletedAt = deletedAt?.toInstant(),
                contributor = contributor,
                html = docAndHtml.html,
                body = docAndHtml.body
            )
        }

        return CommentPage(commentViews, page, size, total)
    }

    fun edit(request: EditRequest): CommentView {
        securityService.validateVerified(request.authCorrelationId)
        val author = fetchContributor(request.authCorrelationId)
            ?: throw ForbiddenException("User must be registered to comment")

        val bodyJson = sanitizeAndValidateBody(request.body)
        val now = OffsetDateTime.now(ZoneOffset.UTC)

        return dsl.transactionResult { _ ->
            val commentRecord = dsl.selectFrom(POST_COMMENT)
                .where(POST_COMMENT.ID.eq(request.commentId))
                .and(POST_COMMENT.POST_ID.eq(request.postId))
                .forUpdate()
                .fetchOne() ?: throw NotFoundException("Comment not found")

            if (commentRecord.contributorId != author.id) {
                throw ForbiddenException("Only the author can edit the comment")
            }
            if (commentRecord.deletedAt != null) {
                throw ClientErrorException("Comment already deleted", Response.Status.CONFLICT)
            }
            val currentEditCount = commentRecord.editCount ?: 0
            if (currentEditCount >= MAX_EDITS) {
                throw ClientErrorException("Comment edit limit reached", Response.Status.CONFLICT)
            }

            val revisionRecord = dsl.newRecord(POST_COMMENT_REVISION)
            revisionRecord.commentId = commentRecord.id
            revisionRecord.bodyJsonPrev = commentRecord.bodyJson
            revisionRecord.bodyTextPrev = commentRecord.bodyText
            revisionRecord.editedAt = now
            revisionRecord.store()

            commentRecord.bodyJson = JSONB.valueOf(objectMapper.writeValueAsString(bodyJson.body))
            commentRecord.bodyText = bodyJson.bodyText
            commentRecord.editedAt = now
            commentRecord.editCount = currentEditCount + 1
            commentRecord.store()

            bodyJson.toView(
                commentRecord.id,
                commentRecord.createdAt?.toInstant() ?: now.toInstant(),
                edited = true,
                deleted = false,
                deletedAt = null,
                contributor = CommentContributor(author.id, author.handle)
            )
        }
    }

    fun softDelete(request: DeleteRequest) {
        securityService.validateVerified(request.authCorrelationId)
        val author = fetchContributor(request.authCorrelationId)
            ?: throw ForbiddenException("User must be registered to comment")

        val commentRecord = dsl.selectFrom(POST_COMMENT)
            .where(POST_COMMENT.ID.eq(request.commentId))
            .and(POST_COMMENT.POST_ID.eq(request.postId))
            .forUpdate()
            .fetchOne() ?: throw NotFoundException("Comment not found")

        if (commentRecord.contributorId != author.id) {
            throw ForbiddenException("Only the author can delete the comment")
        }
        if (commentRecord.deletedAt != null) {
            return
        }

        commentRecord.deletedAt = OffsetDateTime.now(ZoneOffset.UTC)
        commentRecord.deletedByAuthor = true
        commentRecord.store()
    }

    private fun sanitizeAndValidateBody(doc: Doc): DocValidation {
        val trimmedText = BodyTextSerializer.render(doc).trim()
        if (trimmedText.isBlank()) {
            throw BadRequestException("Comment must contain meaningful text")
        }
        if (trimmedText.length > MAX_LENGTH) {
            throw BadRequestException("Comment exceeds maximum length")
        }
        val html = HtmlSanitizer.sanitize(BodyHtmlSerializer.render(doc))
        return DocValidation(doc, trimmedText, html)
    }

    private fun enforceRateLimit(contributorId: UUID) {
        val now = OffsetDateTime.now(ZoneOffset.UTC)
        val burstWindow = now.minusSeconds(BURST_WINDOW_SECONDS)
        val recentCount = dsl.selectCount()
            .from(POST_COMMENT)
            .where(POST_COMMENT.CONTRIBUTOR_ID.eq(contributorId))
            .and(POST_COMMENT.CREATED_AT.gt(burstWindow))
            .fetchOne(0, Int::class.java) ?: 0
        if (recentCount >= BURST_CAPACITY) {
            throw throttleException()
        }
        val slidingWindow = now.minusMinutes(SLIDING_WINDOW_MINUTES.toLong())
        val windowCount = dsl.selectCount()
            .from(POST_COMMENT)
            .where(POST_COMMENT.CONTRIBUTOR_ID.eq(contributorId))
            .and(POST_COMMENT.CREATED_AT.gt(slidingWindow))
            .fetchOne(0, Int::class.java) ?: 0
        if (windowCount >= MAX_WINDOW_EVENTS) {
            throw throttleException()
        }
    }

    private fun throttleException(): WebApplicationException {
        return WebApplicationException(
            Response.status(Response.Status.TOO_MANY_REQUESTS)
                .header("Retry-After", RETRY_AFTER_SECONDS)
                .build()
        )
    }

    private fun ensurePostExists(postId: UUID) {
        val exists = dsl.fetchExists(
            DSL.selectOne().from(POST).where(POST.ID.eq(postId))
        )
        if (!exists) {
            throw NotFoundException("Post not found")
        }
    }

    private fun fetchContributor(authCorrelationId: String): ContributorContext? {
        return dsl.select(CONTRIBUTOR.ID, CONTRIBUTOR.HANDLE)
            .from(CONTRIBUTOR)
            .where(CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq(authCorrelationId))
            .fetchOne { record ->
                ContributorContext(
                    record.get(CONTRIBUTOR.ID),
                    record.get(CONTRIBUTOR.HANDLE)
                )
            }
    }

    data class CreateRequest(val authCorrelationId: String, val postId: UUID, val body: Doc)
    data class ListRequest(val postId: UUID, val page: Int, val pageSize: Int, val authCorrelationId: String? = null)
    data class EditRequest(val authCorrelationId: String, val postId: UUID, val commentId: UUID, val body: Doc)
    data class DeleteRequest(val authCorrelationId: String, val postId: UUID, val commentId: UUID)

    data class CommentPage(val items: List<CommentView>, val page: Int, val pageSize: Int, val total: Int)

    data class CommentView(
        val id: UUID,
        val createdAt: Instant,
        val edited: Boolean,
        val deleted: Boolean,
        val deletedAt: Instant?,
        val contributor: CommentContributor,
        val html: String,
        val body: Doc?
    )

    data class CommentContributor(val id: UUID, val handle: String)

    private data class ContributorContext(val id: UUID, val handle: String)

    private data class DocValidation(val body: Doc, val bodyText: String, val html: String) {
        fun toView(
            id: UUID,
            createdAt: Instant,
            edited: Boolean,
            deleted: Boolean,
            deletedAt: Instant?,
            contributor: CommentContributor
        ): CommentView {
            return CommentView(
                id = id,
                createdAt = createdAt,
                edited = edited,
                deleted = deleted,
                deletedAt = deletedAt,
                contributor = contributor,
                html = html,
                body = body
            )
        }
    }

    private data class DocHtml(val body: Doc?, val html: String)

    companion object {
        private const val MAX_LENGTH = 2000
        private const val MAX_EDITS = 2
        private const val BURST_CAPACITY = 2
        private const val BURST_WINDOW_SECONDS = 30L
        private const val SLIDING_WINDOW_MINUTES = 5
        private const val MAX_WINDOW_EVENTS = (SLIDING_WINDOW_MINUTES * 60 / BURST_WINDOW_SECONDS).toInt()
        private const val RETRY_AFTER_SECONDS = 30
        private const val MAX_PAGE_SIZE = 100
        private const val DELETED_PLACEHOLDER_HTML = "<p>Comment deleted by author</p>"
    }
}
