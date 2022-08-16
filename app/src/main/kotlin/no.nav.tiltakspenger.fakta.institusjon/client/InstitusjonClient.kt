package no.nav.tiltakspenger.fakta.institusjon.client

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.contentType
import no.nav.tiltakspenger.fakta.institusjon.Configuration
import no.nav.tiltakspenger.fakta.institusjon.azure.OauthConfig
import no.nav.tiltakspenger.fakta.institusjon.azure.azureClient
import no.nav.tiltakspenger.fakta.institusjon.model.InstitusjonsOpphold

object KunneIkkeHenteInstitusjonsopphold

class InstitusjonClient(
    private val client: HttpClient = azureClient(
        OauthConfig.fromEnv(
            scope = Configuration.getInstScope()
        )
    )
) {

    suspend fun hentInstitusjonsOpphold(ident: String, behovId: String): List<InstitusjonsOpphold> =
        client.get(Configuration.getInstUrl()) {
            accept(ContentType.Application.Json)
            header("Nav-Call-Id", behovId)
            header("Nav-Consumer-Id", "TILTAKSPENGER")
            header("Med-Institusjonsinformasjon", true)
            header("Nav-Personident", ident)
            contentType(ContentType.Application.Json)
        }.body()

//    private suspend fun hentInstOpphold(ident: String, behovId: String) : Either<KunneIkkeHenteInstitusjonsopphold, List<InstitusjonsOpphold>> {
//        return kotlin.runCatching {
//            client.get(Configuration.getInstUrl()) {
//                accept(ContentType.Application.Json)
//                header("Nav-Call-Id", behovId)
//                header("Nav-Consumer-Id", "TILTAKSPENGER")
//                header("Med-Institusjonsinformasjon", true)
//                header("Nav-Personident", ident)
//                contentType(ContentType.Application.Json)
//            }.body<List<InstitusjonsOpphold>>()
//        }.fold(
//            { it.right() },
//            { KunneIkkeHenteInstitusjonsopphold.left() },
//        )
//    }
//
//    suspend fun hentInstitusjonsoppholdFor(ident: String, behovId: String) : Either<KunneIkkeHenteInstitusjonsopphold, List<InstitusjonsOpphold>> {
//        return hentInstOpphold(ident, behovId)
//    }
}
