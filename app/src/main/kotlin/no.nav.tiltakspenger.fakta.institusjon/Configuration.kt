package no.nav.tiltakspenger.fakta.institusjon

internal object Configuration {
    private fun getPropertyValueByEnvironment(devValue: String, prodValue: String): String {
        return when (System.getenv("NAIS_CLUSTER_NAME")) {
            "dev-gcp" -> devValue
            "prod-gcp" -> prodValue
            else -> devValue
        }
    }

    fun getInstUrl(): String = getPropertyValueByEnvironment(
        devValue = "https://inst2.default.svc.nais.local/api/v1/person/institusjonsopphold",
//      devValue = "https://inst2.dev.intern.nav.no/api/v1/person/institusjonsopphold",
        prodValue = "https://inst2.default.svc.nais.local/api/v1/person/institusjonsopphold",
    )

    fun getInstScope(): String = getPropertyValueByEnvironment(
        devValue = "api://dev-fss.pdl.pdl-api/.default",
        prodValue = "api://prod-fss.pdl.pdl-api/.default"
    )

    private val defaultProperties =
        mapOf(
            "RAPID_APP_NAME" to "tiltakspenger-fakta-institusjon",
            "KAFKA_BROKERS" to System.getenv("KAFKA_BROKERS"),
            "KAFKA_CREDSTORE_PASSWORD" to System.getenv("KAFKA_CREDSTORE_PASSWORD"),
            "KAFKA_TRUSTSTORE_PATH" to System.getenv("KAFKA_TRUSTSTORE_PATH"),
            "KAFKA_KEYSTORE_PATH" to System.getenv("KAFKA_KEYSTORE_PATH"),
            "KAFKA_RAPID_TOPIC" to "tpts.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "KAFKA_CONSUMER_GROUP_ID" to "tiltakspenger-fakta-institusjon-v1"
        )

    fun asMap(): Map<String, String> = defaultProperties
}
