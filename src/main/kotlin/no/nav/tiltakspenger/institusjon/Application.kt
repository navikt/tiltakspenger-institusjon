package no.nav.tiltakspenger.institusjon

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.tiltakspenger.institusjon.auth.AzureTokenProvider
import no.nav.tiltakspenger.institusjon.client.InstitusjonClient

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
                instClient = InstitusjonClient(getToken = tokenProvider::getToken),
            )

            register(object : RapidsConnection.StatusListener {
                override fun onStartup(rapidsConnection: RapidsConnection) {
                    log.info { "Starting tiltakspenger-institusjon" }

                    val environmentVariables = System.getenv()
                    log.info("Dette er proxy-innstillingene")
                    log.info("HTTP_PROXY=${environmentVariables["HTTP_PROXY"]}")
                    log.info("HTTPS_PROXY=${environmentVariables["HTTPS_PROXY"]}")
                    log.info("NO_PROXY=${environmentVariables["NO_PROXY"]}")
                }

                override fun onShutdown(rapidsConnection: RapidsConnection) {
                    log.info { "Stopping tiltakspenger-institusjon" }
                    super.onShutdown(rapidsConnection)
                }
            })
        }.start()
}
