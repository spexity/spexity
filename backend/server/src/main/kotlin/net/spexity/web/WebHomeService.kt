package net.spexity.web

import jakarta.enterprise.context.ApplicationScoped
import net.spexity.data.model.public_.Tables.*
import net.spexity.security.SecurityService
import net.spexity.web.model.CommunityRef
import net.spexity.web.model.ContributorRef
import net.spexity.web.model.PostPreview
import org.jooq.DSLContext
import org.jooq.Record11
import org.jooq.SelectJoinStep
import org.jooq.impl.DSL
import java.time.OffsetDateTime
import java.util.*

@ApplicationScoped
class WebHomeService(
    private val dslContext: DSLContext,
    private val securityService: SecurityService,
) {

    fun homeList(authCorrelationId: String): HomePageData {
        val contributorId = securityService.getContributorId(authCorrelationId)
        val selected = postViewSelect()
            .where(
                POST.COMMUNITY_ID.`in`(
                    DSL.select(CONTRIBUTOR_COMMUNITY.COMMUNITY_ID)
                        .from(CONTRIBUTOR_COMMUNITY)
                        .where(CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID.eq(contributorId))
                )
            )
            .fetch { mapToPostView(it) }
        return HomePageData(selected)
    }

    fun discoverList(authCorrelationId: String?): HomePageData {
        val select = postViewSelect()
        if (authCorrelationId != null) {
            val contributorId = securityService.getContributorId(authCorrelationId)
            select.where(
                POST.COMMUNITY_ID.notIn(
                    DSL.select(CONTRIBUTOR_COMMUNITY.COMMUNITY_ID)
                        .from(CONTRIBUTOR_COMMUNITY)
                        .where(CONTRIBUTOR_COMMUNITY.CONTRIBUTOR_ID.eq(contributorId))
                )
            )
        }
        val selected = select
            .fetch { mapToPostView(it) }
        return HomePageData(selected)
    }

    private fun mapToPostView(record: Record11<UUID, OffsetDateTime, String, String, UUID, String, String, String, UUID, String, Int>): PostPreview {
        val instant = record.get(POST.CREATED_AT).toInstant()
        return PostPreview(
            record.get(POST.ID),
            instant,
            record.get(POST.SUBJECT),
            record.get(POST.BODY_TEXT).take(512),
            ContributorRef(
                record.get(POST.CONTRIBUTOR_ID),
                record.get(POST.contributor().HANDLE),
                record.get(POST.contributor().AVATAR_TEXT),
                record.get(POST.contributor().AVATAR_BG_COLOR),
            ),
            CommunityRef(record.get(POST.COMMUNITY_ID), record.get(COMMUNITY.NAME)),
            record.get(POST.COMMENTS_COUNT)
        )
    }

    fun postViewSelect(): SelectJoinStep<Record11<UUID, OffsetDateTime, String, String, UUID, String, String, String, UUID, String, Int>> {
        return dslContext.select(
            POST.ID,
            POST.CREATED_AT,
            POST.SUBJECT,
            POST.BODY_TEXT,
            POST.CONTRIBUTOR_ID,
            POST.contributor().HANDLE,
            POST.contributor().AVATAR_TEXT,
            POST.contributor().AVATAR_BG_COLOR,
            POST.COMMUNITY_ID,
            POST.community().NAME,
            POST.COMMENTS_COUNT,
        )
            .from(POST)
    }

    data class HomePageData(val posts: List<PostPreview>)


}