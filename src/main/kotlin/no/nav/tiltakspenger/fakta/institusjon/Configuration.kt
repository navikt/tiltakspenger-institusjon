package no.nav.tiltakspenger.fakta.institusjon

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

object Configuration {
//    private fun getPropertyValueByEnvironment(devValue: String, prodValue: String): String {
//        return when (System.getenv("NAIS_CLUSTER_NAME")) {
//            "dev-fss" -> devValue
//            "prod-fss" -> prodValue
//            else -> devValue
//        }
//    }
//
//    fun getInstUrl(): String = getPropertyValueByEnvironment(
// //        devValue = "https://inst2.default.svc.nais.local/api/v1/person/institusjonsopphold",
//        devValue = "https://inst2.dev.intern.nav.no/api/v1/person/institusjonsopphold",
//        prodValue = "https://inst2.default.svc.nais.local/api/v1/person/institusjonsopphold",
//    )
//
//    fun getInstScope(): String = getPropertyValueByEnvironment(
//        devValue = "api://dev-gcp.team-rocket.pip-api/.default",
//        prodValue = "api://prod-fss.team-rocket.inst2/.default"
//    )

    val rapidsAndRivers = mapOf(
        "RAPID_APP_NAME" to "tiltakspenger-fakta-institusjon",
        "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
        "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
        "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
        "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
        "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
        "KAFKA_RESET_POLICY" to "latest",
        "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-fakta-institusjon-v1",
    )

    private val otherDefaultProperties = mapOf(
        "application.httpPort" to 8080.toString(),
//        "SERVICEUSER_TPTS_USERNAME" to System.getenv("SERVICEUSER_TPTS_USERNAME"),
//        "SERVICEUSER_TPTS_PASSWORD" to System.getenv("SERVICEUSER_TPTS_PASSWORD"),
        "AZURE_APP_CLIENT_ID" to System.getenv("AZURE_APP_CLIENT_ID"),
        "AZURE_APP_CLIENT_SECRET" to System.getenv("AZURE_APP_CLIENT_SECRET"),
        "AZURE_APP_WELL_KNOWN_URL" to System.getenv("AZURE_APP_WELL_KNOWN_URL"),
    )
    private val defaultProperties = ConfigurationMap(rapidsAndRivers + otherDefaultProperties)

    private val localProperties = ConfigurationMap(
        mapOf(
//            "url" to "",
            "application.profile" to Profile.LOCAL.toString(),
            "instScope" to "api://dev-fss.team-rocket.inst2/.default",
            "instBaseUrl" to "https://inst2.dev.intern.nav.no",
        )
    )
    private val devProperties = ConfigurationMap(
        mapOf(
//            "stsUrl" to "",
            "application.profile" to Profile.DEV.toString(),
            "instScope" to "api://dev-fss.team-rocket.inst2/.default",
            "instBaseUrl" to "http://inst2.team-rocket.svc.nais.local",
        )
    )
    private val prodProperties = ConfigurationMap(
        mapOf(
//            "url" to "",
            "application.profile" to Profile.PROD.toString(),
            "instScope" to "api://prod-fss.team-rocket.inst2/.default",
            "instBaseUrl" to "http://inst2.team-rocket.svc.nais.local",
        )
    )

    private fun config() = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding devProperties overriding defaultProperties
        "prod-gcp" ->
            systemProperties() overriding EnvironmentVariables overriding prodProperties overriding defaultProperties
        else -> {
            systemProperties() overriding EnvironmentVariables overriding localProperties overriding defaultProperties
        }
    }

    data class OauthConfig(
        val scope: String = config()[Key("instScope", stringType)],
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        val wellknownUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
    )

    data class InstClientConfig(
        val baseUrl: String = config()[Key("instBaseUrl", stringType)],
    )

//    private val defaultProperties =
//        mapOf(
//            "RAPID_APP_NAME" to "tiltakspenger-fakta-institusjon",
//            "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
//            "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
//            "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
//            "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
//            "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
//            "KAFKA_RESET_POLICY" to "latest",
//            "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-fakta-institusjon-v1"
//        )

//    fun asMap(): Map<String, String> = defaultProperties
}

enum class Profile {
    LOCAL, DEV, PROD
}
