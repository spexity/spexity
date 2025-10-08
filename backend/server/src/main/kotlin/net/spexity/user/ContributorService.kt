package net.spexity.user

import jakarta.enterprise.context.ApplicationScoped
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.CONTRIBUTOR_ALIAS_META
import net.spexity.data.model.public_.routines.PickDiscriminator
import net.spexity.data.model.public_.tables.records.ContributorRecord
import org.jboss.logging.Logger
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

private const val UNIQUE_EXCEPTION = "23505"

@ApplicationScoped
class ContributorService(private val dslContext: DSLContext, private val logger: Logger) {

    fun register(request: RegRequest): RegResponse {
        return withRetryDigits(request.alias, currentDigits(request.alias)) { candidate ->
            tryInsertContributor(request, candidate)
        }
    }

    fun update(request: UpdateRequest): RegResponse {
        val currentAlias = dslContext.select(CONTRIBUTOR.ALIAS)
            .from(CONTRIBUTOR)
            .where(CONTRIBUTOR.ID.eq(request.id))
            .fetchOne(CONTRIBUTOR.ALIAS) ?: error("Contributor not found")
        if (currentAlias == request.alias) {
            val updated = dslContext.update(CONTRIBUTOR)
                .set(CONTRIBUTOR.AVATAR_TEXT, request.avatarText)
                .set(CONTRIBUTOR.AVATAR_BG_COLOR, request.avatarBgColor)
                .where(CONTRIBUTOR.ID.eq(request.id))
                .returning()
                .fetchOne() ?: error("Failed to update contributor")
            return RegResponse(updated.get(CONTRIBUTOR.ID), updated.get(CONTRIBUTOR.HANDLE))
        }
        return withRetryDigits(request.alias, currentDigits(request.alias)) { candidate ->
            tryUpdateContributor(request, candidate)
        }
    }

    private fun currentDigits(alias: String): Int =
        dslContext.select(CONTRIBUTOR_ALIAS_META.DIGITS)
            .from(CONTRIBUTOR_ALIAS_META)
            .where(CONTRIBUTOR_ALIAS_META.ALIAS.eq(alias))
            .fetchOne(CONTRIBUTOR_ALIAS_META.DIGITS) ?: 4

    private tailrec fun withRetryDigits(alias: String, digits: Int, op: (Int) -> ContributorRecord?): RegResponse {
        // Stage 1: try pure-random inserts (fast path)
        repeat(8) {
            val candidate = randomInRange(digits)
            val inserted = op(candidate)
            if (inserted != null) {
                return RegResponse(inserted.get(CONTRIBUTOR.ID), inserted.get(CONTRIBUTOR.HANDLE))
            }
        }
        // Stage 2: deterministic db-side selection under an advisory lock
        val candidate = pickDiscriminatorInDb(alias, digits)
        if (candidate != null) {
            val inserted = op(candidate)
            if (inserted != null) {
                return RegResponse(inserted.get(CONTRIBUTOR.ID), inserted.get(CONTRIBUTOR.HANDLE))
            }
        }
        val nextDigits = digits + 1
        logger.info("Alias $alias is full of $digits's , updating to $nextDigits's")
        upsertAliasMeta(alias, nextDigits)
        return withRetryDigits(alias, nextDigits, op)
    }

    private fun pickDiscriminatorInDb(alias: String, digits: Int): Int? {
        val dbFun = PickDiscriminator()
        dbFun.setPAlias(alias)
        dbFun.setPDigits(digits)
        dbFun.execute(dslContext.configuration())
        return dbFun.returnValue
    }

    private fun tryInsertContributor(request: RegRequest, candidate: Int): ContributorRecord? =
        dslContext.insertInto(CONTRIBUTOR)
            .set(CONTRIBUTOR.USER_ACCOUNT_ID, request.userAccountId)
            .set(CONTRIBUTOR.ALIAS, request.alias)
            .set(CONTRIBUTOR.AVATAR_TEXT, request.avatarText)
            .set(CONTRIBUTOR.AVATAR_BG_COLOR, request.avatarBgColor)
            .set(CONTRIBUTOR.DISCRIMINATOR, candidate)
            .onConflictDoNothing()
            .returning()
            .fetchOne()

    private fun tryUpdateContributor(request: UpdateRequest, candidate: Int): ContributorRecord? =
        try {
            dslContext.update(CONTRIBUTOR)
                .set(CONTRIBUTOR.ALIAS, request.alias)
                .set(CONTRIBUTOR.AVATAR_TEXT, request.avatarText)
                .set(CONTRIBUTOR.AVATAR_BG_COLOR, request.avatarBgColor)
                .set(CONTRIBUTOR.DISCRIMINATOR, candidate)
                .where(CONTRIBUTOR.ID.eq(request.id))
                .returning()
                .fetchOne()
        } catch (e: DataAccessException) {
            if (e.sqlState() == UNIQUE_EXCEPTION) null else throw e
        }

    private fun upsertAliasMeta(alias: String, digits: Int) {
        if (digits < 5) {
            return
        }
        dslContext.insertInto(CONTRIBUTOR_ALIAS_META)
            .columns(CONTRIBUTOR_ALIAS_META.ALIAS, CONTRIBUTOR_ALIAS_META.DIGITS)
            .values(alias, digits)
            .onConflict(CONTRIBUTOR_ALIAS_META.ALIAS)
            .doUpdate()
            .set(CONTRIBUTOR_ALIAS_META.DIGITS, digits)
            .execute()
    }

    private fun randomInRange(digits: Int): Int {
        val min = 10.0.pow((digits - 1)).toInt()
        val max = 10.0.pow(digits).toInt() - 1
        return ThreadLocalRandom.current().nextInt(min, max + 1)
    }

    data class RegRequest(
        val userAccountId: UUID, val alias: String,
        val avatarText: String, val avatarBgColor: String
    )

    data class UpdateRequest(
        val id: UUID, val alias: String,
        val avatarText: String, val avatarBgColor: String
    )

    data class RegResponse(val id: UUID, val handle: String)

}
