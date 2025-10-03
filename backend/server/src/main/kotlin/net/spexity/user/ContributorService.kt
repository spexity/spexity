package net.spexity.user

import jakarta.enterprise.context.ApplicationScoped
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.CONTRIBUTOR_ALIAS_META
import net.spexity.data.model.public_.routines.PickDiscriminator
import net.spexity.data.model.public_.tables.records.ContributorRecord
import org.jboss.logging.Logger
import org.jooq.DSLContext
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

@ApplicationScoped
class ContributorService(private val dslContext: DSLContext, private val logger: Logger) {

    fun register(request: RegRequest): RegResponse = withRetryDigits(request, currentDigits(request.alias))

    private fun currentDigits(alias: String): Int =
        dslContext.select(CONTRIBUTOR_ALIAS_META.DIGITS)
            .from(CONTRIBUTOR_ALIAS_META)
            .where(CONTRIBUTOR_ALIAS_META.ALIAS.eq(alias))
            .fetchOne(CONTRIBUTOR_ALIAS_META.DIGITS) ?: 4

    private tailrec fun withRetryDigits(request: RegRequest, digits: Int): RegResponse {
        // Stage 1: try pure-random inserts (fast path)
        repeat(8) {
            val candidate = randomInRange(digits)
            val inserted = tryInsertContributor(request, candidate)
            if (inserted != null) {
                return RegResponse(inserted.get(CONTRIBUTOR.ID), inserted.get(CONTRIBUTOR.HANDLE))
            }
        }
        // Stage 2: deterministic db-side selection under an advisory lock
        val candidate = pickDiscriminatorInDb(request, digits)
        if (candidate != null) {
            val inserted = tryInsertContributor(request, candidate)
            if (inserted != null) {
                return RegResponse(inserted.get(CONTRIBUTOR.ID), inserted.get(CONTRIBUTOR.HANDLE))
            }
        }
        val nextDigits = digits + 1
        logger.info("Alias ${request.alias} is full of $digits's , updating to $nextDigits's")
        upsertAliasMeta(request.alias, nextDigits)
        return withRetryDigits(request, nextDigits)
    }

    private fun pickDiscriminatorInDb(request: RegRequest, digits: Int): Int? {
        val dbFun = PickDiscriminator()
        dbFun.setPAlias(request.alias)
        dbFun.setPDigits(digits)
        dbFun.execute(dslContext.configuration())
        return dbFun.returnValue
    }

    private fun tryInsertContributor(request: RegRequest, candidate: Int): ContributorRecord? =
        dslContext.insertInto(CONTRIBUTOR)
            .set(CONTRIBUTOR.USER_ACCOUNT_ID, request.userAccountId)
            .set(CONTRIBUTOR.ALIAS, request.alias)
            .set(CONTRIBUTOR.AVATAR_EMOJI, request.avatarEmojis)
            .set(CONTRIBUTOR.AVATAR_BG_COLOR, request.avatarBgColor)
            .set(CONTRIBUTOR.DISCRIMINATOR, candidate)
            .onConflictDoNothing()
            .returning()
            .fetchOne()

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
        val avatarEmojis: String, val avatarBgColor: String
    )

    data class RegResponse(val id: UUID, val handle: String)

}
