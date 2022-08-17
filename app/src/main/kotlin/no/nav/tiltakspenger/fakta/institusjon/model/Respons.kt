package no.nav.tiltakspenger.fakta.institusjon.model

data class Respons(
    val opphold: List<InstitusjonsOpphold>,
    val feilmelding: String?,
)
