package net.spexity.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Configuration
@EnableMethodSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(HttpMethod.GET, "/api/posts/*/comments/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/web/**").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.opaqueToken { opaque ->
                    opaque.introspector(introspector())
                }
            }
        return http.build()
    }

    @Bean
    fun introspector(): OpaqueTokenIntrospector = LocalOpaqueTokenIntrospector()
}

class LocalOpaqueTokenIntrospector : OpaqueTokenIntrospector {
    override fun introspect(token: String): OAuth2AuthenticatedPrincipal {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_user"))
        val attributes = mapOf(
            "sub" to token,
            "email" to resolveEmail(token)
        )
        return DefaultOAuth2AuthenticatedPrincipal(token, attributes, authorities)
    }

    private fun resolveEmail(token: String): String {
        val request = (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request
        return request?.getHeader("X-Auth-Email") ?: "$token@example.com"
    }
}
