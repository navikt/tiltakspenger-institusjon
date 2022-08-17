package no.nav.tiltakspenger.fakta.institusjon.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.tiltakspenger.fakta.institusjon.defaultHttpClient
import no.nav.tiltakspenger.fakta.institusjon.defaultObjectMapper
import no.nav.tiltakspenger.fakta.institusjon.model.InstitusjonsOpphold
import no.nav.tiltakspenger.fakta.institusjon.Configuration

class InstitusjonClient(
    private val skjermingConfig: Configuration.InstClientConfig = Configuration.InstClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine = CIO.create(),
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine
    ) {
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(
                        accessToken = getToken(),
                        // Refresh token are used in refreshToken method if client gets 401
                        // Should't need this if token expiry is checked first
                        refreshToken = emptyRefreshToken,
                    )
                }
            }
        }
    }
) {
    companion object {
        const val emptyRefreshToken = ""
        const val navCallIdHeader = "Nav-Call-Id"
        const val navConsumerId = "Nav-Consumer-Id"
        const val navPersonIdent = "Nav-Personident"
        const val medInstInformasjon = "Med-Institusjonsinformasjon"
    }

    suspend fun hentInstitusjonsOpphold(ident: String, behovId: String): List<InstitusjonsOpphold> =
        httpClient.get("${skjermingConfig.baseUrl}/api/v1/person/institusjonsopphold") {
            accept(ContentType.Application.Json)
            header(navCallIdHeader, behovId)
            header(navConsumerId, "TILTAKSPENGER")
            header(medInstInformasjon, true)
            header(navPersonIdent, ident)
            contentType(ContentType.Application.Json)
        }.body()
}
