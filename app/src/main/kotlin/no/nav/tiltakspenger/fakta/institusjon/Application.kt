package no.nav.tiltakspenger.fakta.institusjon

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.fakta.institusjon.oauth.AzureTokenProvider
import no.nav.tiltakspenger.fakta.institusjon.client.InstitusjonClient

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        LOG.error { "Uncaught exception logget i securelog" }
        SECURELOG.error(e) { e.message }
    }

    val tokenProvider = AzureTokenProvider()

    RapidApplication.create(Configuration.rapidsAndRivers)
        .apply {
            InstitusjonService(
                rapidsConnection = this,
                instClient = InstitusjonClient(getToken = tokenProvider::getToken)
            )

            register(object : RapidsConnection.StatusListener {
                override fun onStartup(rapidsConnection: RapidsConnection) {
                    LOG.info { "Starting tiltakspenger-fakta-institusjon" }
                }

                override fun onShutdown(rapidsConnection: RapidsConnection) {
                    LOG.info { "Stopping tiltakspenger-fakta-institusjon" }
                    super.onShutdown(rapidsConnection)
                }
            })
        }.start()
}
