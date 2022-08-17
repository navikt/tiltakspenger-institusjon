package no.nav.tiltakspenger.fakta.institusjon

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.tiltakspenger.fakta.institusjon.client.InstitusjonClient
import no.nav.tiltakspenger.fakta.institusjon.oauth.AzureTokenProvider

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        LOG.error { "Uncaught exception logget i securelog" }
        SECURELOG.error(e) { e.message }
    }

    val tokenProvider = AzureTokenProvider()

    RapidApplication.create(Configuration.rapidsAndRivers)
        .also {
            InstitusjonService(
                rapidsConnection = it,
                instClient = InstitusjonClient(getToken = tokenProvider::getToken)
            )
        }.start()
}
