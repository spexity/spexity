package net.spexity.user

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.USER_ACCOUNT
import net.spexity.web.model.ContributorRef
import org.jooq.DSLContext
import java.util.*

@ApplicationScoped
class UserService(private val dslContext: DSLContext, private val contributorService: ContributorService) {

    fun register(request: RegRequest): RegResponse {
        if (hasRegistered(request.authCorrelationId)) {
            throw BadRequestException("Already registered")
        }
        return dslContext.transactionResult { _ ->
            val account = dslContext.insertInto(USER_ACCOUNT)
                .set(USER_ACCOUNT.AUTH_CORRELATION_ID, request.authCorrelationId)
                .set(USER_ACCOUNT.IS_VERIFIED_HUMAN, false)
                .set(USER_ACCOUNT.EMAIL_ADDRESS, request.emailAddress)
                .returning()
                .fetchOne()!!
            val contributor = contributorService.register(
                ContributorService.RegRequest(
                    account.id, request.alias,
                    request.avatarText, request.avatarBgColor
                )
            )
            RegResponse(
                account.id,
                account.isVerifiedHuman,
                request.authCorrelationId,
                ContributorRef(
                    contributor.id,
                    contributor.handle,
                    request.avatarText,
                    request.avatarBgColor
                )
            )
        }
    }

    fun getUser(authCorrelationId: String): RegResponse? {
        val result = dslContext.select(
            CONTRIBUTOR.userAccount().ID, CONTRIBUTOR.userAccount().IS_VERIFIED_HUMAN,
            CONTRIBUTOR.ID, CONTRIBUTOR.HANDLE,
            CONTRIBUTOR.AVATAR_TEXT, CONTRIBUTOR.AVATAR_BG_COLOR
        )
            .from(CONTRIBUTOR)
            .where(CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq(authCorrelationId))
            .fetch()
        if (result.isEmpty()) {
            return null
        }
        return result[0].map {
            RegResponse(
                it.get(CONTRIBUTOR.userAccount().ID),
                it.get(CONTRIBUTOR.userAccount().IS_VERIFIED_HUMAN),
                authCorrelationId,
                ContributorRef(
                    it.get(CONTRIBUTOR.ID),
                    it.get(CONTRIBUTOR.HANDLE),
                    it.get(CONTRIBUTOR.AVATAR_TEXT),
                    it.get(CONTRIBUTOR.AVATAR_BG_COLOR)
                )
            )
        }
    }

    fun hasRegistered(authCorrelationId: String): Boolean {
        val count = dslContext
            .selectCount()
            .from(USER_ACCOUNT)
            .where(USER_ACCOUNT.AUTH_CORRELATION_ID.eq(authCorrelationId))
            .fetchOne(0, Int::class.java)!!
        return count > 0
    }

    fun update(request: UpdateRequest): RegResponse {
        val result = dslContext.select(
            CONTRIBUTOR.userAccount().ID, CONTRIBUTOR.userAccount().IS_VERIFIED_HUMAN,
            CONTRIBUTOR.ID
        )
            .from(CONTRIBUTOR)
            .where(CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq(request.authCorrelationId))
            .fetch()
        if (result.isEmpty()) {
            throw BadRequestException("Not registered yet")
        }
        val updatedContributor = contributorService.update(
            ContributorService.UpdateRequest(
                result[0].get(CONTRIBUTOR.ID), request.alias, request.avatarText, request.avatarBgColor
            )
        )
        return result[0].map {
            RegResponse(
                it.get(CONTRIBUTOR.userAccount().ID),
                it.get(CONTRIBUTOR.userAccount().IS_VERIFIED_HUMAN),
                request.authCorrelationId,
                ContributorRef(
                    updatedContributor.id,
                    updatedContributor.handle,
                    request.avatarText,
                    request.avatarBgColor
                )
            )
        }
    }

    data class RegRequest(
        val authCorrelationId: String,
        val emailAddress: String,
        val alias: String,
        val avatarText: String,
        val avatarBgColor: String
    )

    data class UpdateRequest(
        val authCorrelationId: String,
        val alias: String,
        val avatarText: String,
        val avatarBgColor: String
    )

    data class RegResponse(
        val id: UUID,
        val isVerifiedHuman: Boolean,
        val authCorrelationId: String,
        val contributor: ContributorRef
    )

}
