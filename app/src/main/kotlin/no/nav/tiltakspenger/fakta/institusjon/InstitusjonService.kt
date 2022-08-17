package no.nav.tiltakspenger.fakta.institusjon

import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.logstash.logback.argument.StructuredArguments
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.withMDC
import no.nav.tiltakspenger.fakta.institusjon.client.InstitusjonClient
import no.nav.tiltakspenger.fakta.institusjon.model.Respons

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

class InstitusjonService(
    rapidsConnection: RapidsConnection,
    private val instClient: InstitusjonClient,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate {
                it.demandAllOrAny("@behov", listOf("institusjon"))
                it.forbid("@løsning")
                it.requireKey("@id", "@behovId")
                it.requireKey("ident")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        kotlin.runCatching {
            loggVedInngang(packet)

            val behovId = packet["@behovId"]
            withMDC(
                "behovId" to packet["@behovId"].asText(),
            ) {
                val fnr = packet["ident"].asText()
                runBlocking {
                    instClient.hentInstitusjonsOpphold(fnr, behovId.asText())
                }.let { response ->
                    packet["@løsning"] = Respons(opphold = response, feilmelding = null)
                    loggVedUtgang(packet) { response.toString() }
                    context.publish(packet.toJson())
                }
            }
        }.onFailure {
            loggVedFeil(it, packet)
        }.getOrThrow()
    }

    fun loggVedInngang(packet: JsonMessage) {
        LOG.info(
            "løser behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.info(
            "løser behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.debug { "mottok melding: ${packet.toJson()}" }
    }

    private fun loggVedUtgang(packet: JsonMessage, løsning: () -> String) {
        LOG.info(
            "har løst behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.info(
            "har løst behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        SECURELOG.debug { "publiserer løsning: $løsning" }
    }

    private fun loggVedFeil(ex: Throwable, packet: JsonMessage) {
        LOG.error(
            "feil ved behandling av behov med {}, se securelogs for detaljer",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
        )
        LOG.error(
            "feil ${ex.message} ved behandling av behov med {} og {}",
            StructuredArguments.keyValue("id", packet["@id"].asText()),
            StructuredArguments.keyValue("behovId", packet["@behovId"].asText()),
            ex,
        )
    }
}
