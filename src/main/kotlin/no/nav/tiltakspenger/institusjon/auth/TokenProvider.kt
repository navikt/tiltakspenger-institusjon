package no.nav.tiltakspenger.institusjon.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.http
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.Parameters
import mu.KotlinLogging
import no.nav.tiltakspenger.institusjon.Configuration
import no.nav.tiltakspenger.institusjon.defaultHttpClient
import no.nav.tiltakspenger.institusjon.defaultObjectMapper
import java.time.LocalDateTime

private val LOG = KotlinLogging.logger {}

fun interface TokenProvider {
    suspend fun getToken(): String
}

class AzureTokenProvider(
    objectMapper: ObjectMapper = defaultObjectMapper(),
    engine: HttpClientEngine? = null,
    private val config: OauthConfig = Configuration.oauthConfig(),
) : TokenProvider {
    private val azureHttpClient = defaultHttpClient(objectMapper = objectMapper, engine = engine) {
        System.getenv("HTTP_PROXY")?.let {
            LOG.info("Setter opp proxy mot $it")
            this.proxy = ProxyBuilder.http(it)
        }
    }

    private val tokenCache = TokenCache()

    override suspend fun getToken(): String {
        try {
            val currentToken = tokenCache.token
            return if (currentToken != null && !tokenCache.isExpired()) {
                currentToken
            } else {
                clientCredentials()
            }
        } catch (e: Exception) {
            throw AzureAuthException(e)
        }
    }

    private suspend fun wellknown(): WellKnown {
        return azureHttpClient.get(config.wellknownUrl).body()
    }

    private suspend fun clientCredentials(): String {
        return azureHttpClient.submitForm(
            url = wellknown().tokenEndpoint,
            formParameters = Parameters.build {
                append("grant_type", "client_credentials")
                append("client_id", config.clientId)
                append("client_secret", config.clientSecret)
                append("scope", config.scope)
            },
        ).body<OAuth2AccessTokenResponse>().let {
            tokenCache.update(
                it.accessToken,
                it.expiresIn.toLong(),
            )
            return@let it.accessToken
        }
    }

    class TokenCache {

        var token: String? = null
            private set
        private var expires: LocalDateTime? = null

        fun isExpired(): Boolean = expires?.isBefore(LocalDateTime.now()) ?: true

        fun update(accessToken: String, expiresIn: Long) {
            token = accessToken
            expires = LocalDateTime.now().plusSeconds(expiresIn).minusSeconds(Companion.SAFETYMARGIN)
        }

        companion object {
            const val SAFETYMARGIN: Long = 60
        }
    }

    data class OauthConfig(
        val scope: String,
        val clientId: String,
        val clientSecret: String,
        val wellknownUrl: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class WellKnown(
        @JsonProperty("token_endpoint")
        val tokenEndpoint: String,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class OAuth2AccessTokenResponse(
        @JsonProperty("token_type")
        val tokenType: String,
        @JsonProperty("access_token")
        var accessToken: String,
        @JsonProperty("ext_expires_in")
        val extExpiresIn: Int,
        @JsonProperty("expires_in")
        val expiresIn: Int,
    )

    class AzureAuthException(e: Exception) : RuntimeException(e)
}
