package no.nav.tiltakspenger.fakta.institusjon

import mu.KotlinLogging
import no.nav.helse.rapids_rivers.RapidApplication

private val LOG = KotlinLogging.logger {}
fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e -> LOG.error(e) { e.message } }
    LOG.info { "Starting tiltakspenger-fakta-institusjon" }
    RapidApplication.create(Configuration.asMap())
        .also {
            InstitusjonService(it)
        }.start()
}
