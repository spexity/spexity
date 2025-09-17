package net.spexity.user

import jakarta.enterprise.context.ApplicationScoped
import jakarta.ws.rs.BadRequestException
import net.spexity.data.model.public_.Tables.CONTRIBUTOR
import net.spexity.data.model.public_.Tables.USER_ACCOUNT
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
                .set(USER_ACCOUNT.EMAIL_ADDRESS, request.emailAddress)
                .returning()
                .fetchOne()!!
            val contributor = contributorService.register(ContributorService.RegRequest(account.id, request.alias))
            RegResponse(account.id, request.authCorrelationId, contributor.id, contributor.handle)
        }
    }

    fun getUser(authCorrelationId: String): RegResponse? {
        val result = dslContext.select(CONTRIBUTOR.userAccount().ID, CONTRIBUTOR.ID, CONTRIBUTOR.HANDLE)
            .from(CONTRIBUTOR)
            .where(CONTRIBUTOR.userAccount().AUTH_CORRELATION_ID.eq(authCorrelationId))
            .fetch()
        if (result.isEmpty()) {
            return null
        }
        return result[0].map {
            RegResponse(
                it.get(CONTRIBUTOR.userAccount().ID),
                authCorrelationId,
                it.get(CONTRIBUTOR.ID),
                it.get(CONTRIBUTOR.HANDLE),
            )
        }
    }

    fun registeredAndVerified(authCorrelationId: String): Boolean {
        return hasRegistered(authCorrelationId) //TODO: verification of humanity
    }

    fun hasRegistered(authCorrelationId: String): Boolean {
        val count = dslContext
            .selectCount()
            .from(USER_ACCOUNT)
            .where(USER_ACCOUNT.AUTH_CORRELATION_ID.eq(authCorrelationId))
            .fetchOne(0, Int::class.java)!!
        return count > 0
    }

    data class RegRequest(val authCorrelationId: String, val emailAddress: String, val alias: String)

    data class RegResponse(
        val id: UUID,
        val authCorrelationId: String,
        val contributorId: UUID,
        val contributorHandle: String
    )

}
