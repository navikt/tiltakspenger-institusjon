package no.nav.tiltakspenger.fakta.institusjon.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import no.nav.tiltakspenger.fakta.institusjon.Configuration
import no.nav.tiltakspenger.fakta.institusjon.defaultHttpClient
import no.nav.tiltakspenger.fakta.institusjon.defaultObjectMapper
import no.nav.tiltakspenger.fakta.institusjon.model.InstitusjonsOpphold

class InstitusjonClient(
    private val instConfig: Configuration.InstClientConfig = Configuration.InstClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngineFactory<HttpClientEngineConfig> = CIO,
    private val httpClient: HttpClient = defaultHttpClient(
        objectMapper = objectMapper,
        engine = engine
    )
) {
    companion object {
        const val navCallIdHeader = "Nav-Call-Id"
        const val navConsumerId = "Nav-Consumer-Id"
        const val navPersonIdent = "Nav-Personident"
        const val medInstInformasjon = "Med-Institusjonsinformasjon"
    }

    suspend fun hentInstitusjonsOpphold(ident: String, behovId: String): List<InstitusjonsOpphold> =
        httpClient.get("${instConfig.baseUrl}/api/v1/person/institusjonsopphold") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${getToken.invoke()}")
            header(navCallIdHeader, behovId)
            header(navConsumerId, "TILTAKSPENGER")
            header(medInstInformasjon, true)
            header(navPersonIdent, ident)
            contentType(ContentType.Application.Json)
        }.body()
}
