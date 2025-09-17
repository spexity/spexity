package net.spexity.post

import org.owasp.html.AttributePolicy
import org.owasp.html.HtmlPolicyBuilder
import org.owasp.html.PolicyFactory
import org.owasp.html.Sanitizers


object HtmlSanitizer {

    private val TARGET_BLANK: AttributePolicy = AttributePolicy { _: String?, _: String?, _: String? -> "_blank" }
    private val POLICY: PolicyFactory = Sanitizers.FORMATTING
        .and(Sanitizers.BLOCKS)
        .and(HtmlPolicyBuilder().allowElements("hr", "pre").toFactory())
        .and(
            HtmlPolicyBuilder().allowStandardUrlProtocols().allowElements("a")
                .allowAttributes("href").onElements("a")
                .allowAttributes("target").matching(TARGET_BLANK).onElements("a")
                .requireRelsOnLinks("nofollow", "noopener", "noreferrer")
                .toFactory()
        )

    fun sanitize(html: String): String = POLICY.sanitize(html)

}