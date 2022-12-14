package no.nav.tiltakspenger.institusjon.model

data class Respons(
    val opphold: List<InstitusjonsOpphold>,
    val feilmelding: String?,
)
