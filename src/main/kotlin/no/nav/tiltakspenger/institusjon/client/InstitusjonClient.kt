package no.nav.tiltakspenger.institusjon.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import no.nav.tiltakspenger.institusjon.Configuration
import no.nav.tiltakspenger.institusjon.defaultHttpClient
import no.nav.tiltakspenger.institusjon.defaultObjectMapper
import no.nav.tiltakspenger.institusjon.model.InstitusjonsOpphold

class InstitusjonClient(
    private val instConfig: Configuration.InstClientConfig = Configuration.InstClientConfig(),
    private val objectMapper: ObjectMapper = defaultObjectMapper(),
    private val getToken: suspend () -> String,
    engine: HttpClientEngine? = null,
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
