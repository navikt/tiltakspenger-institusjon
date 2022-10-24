package no.nav.tiltakspenger.fakta.institusjon

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.fakta.institusjon.auth.AzureTokenProvider
import no.nav.tiltakspenger.fakta.institusjon.client.InstitusjonClient

fun main() {
    System.setProperty("logback.configurationFile", "egenLogback.xml")
    val log = KotlinLogging.logger {}
    val securelog = KotlinLogging.logger("tjenestekall")

    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        log.error { "Uncaught exception logget i securelog" }
        securelog.error(e) { e.message }
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
                    log.info { "Starting tiltakspenger-fakta-institusjon" }
                }

                override fun onShutdown(rapidsConnection: RapidsConnection) {
                    log.info { "Stopping tiltakspenger-fakta-institusjon" }
                    super.onShutdown(rapidsConnection)
                }
            })
        }.start()
}
