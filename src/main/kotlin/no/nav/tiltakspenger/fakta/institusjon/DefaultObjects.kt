package no.nav.tiltakspenger.fakta.institusjon

import com.fasterxml.jackson.core.util.DefaultIndenter
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import mu.KotlinLogging
import java.time.Duration

private val LOG = KotlinLogging.logger {}
private val SECURELOG = KotlinLogging.logger("tjenestekall")

private object SecurelogWrapper : Logger {
    override fun log(message: String) {
        LOG.info("HttpClient detaljer logget til securelog")
        //LOG.error("HttpClient feil $message")
        SECURELOG.info(message)
    }
}

@Suppress("MagicNumber")
fun defaultHttpClient(
    objectMapper: ObjectMapper,
    engine: HttpClientEngine = CIO.create {
        this.proxy =
            ProxyBuilder.http(System.getenv("HTTP_PROXY").also { LOG.info { "Setter opp HTTP_PROXY mot $it" } })
    },
    configBlock: HttpClientConfig<*>.() -> Unit = {}
) = HttpClient(engine) {
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
    }
    install(HttpTimeout) {
        connectTimeoutMillis = Duration.ofSeconds(60).toMillis()
        requestTimeoutMillis = Duration.ofSeconds(60).toMillis()
        socketTimeoutMillis = Duration.ofSeconds(60).toMillis()
    }

    this.install(Logging) {
        logger = SecurelogWrapper
        level = LogLevel.ALL
    }
    this.expectSuccess = true
    
    apply(configBlock)
}

fun defaultObjectMapper(): ObjectMapper = ObjectMapper()
    .registerModule(KotlinModule.Builder().build())
    .registerModule(JavaTimeModule())
    .setDefaultPrettyPrinter(
        DefaultPrettyPrinter().apply {
            indentArraysWith(DefaultPrettyPrinter.FixedSpaceIndenter.instance)
            indentObjectsWith(DefaultIndenter("  ", "\n"))
        }
    )
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
