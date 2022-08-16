package no.nav.tiltakspenger.fakta.institusjon.model

import java.time.LocalDate
import java.time.LocalDateTime

data class InstitusjonsOpphold(
    val oppholdId: Long,
    val tssEksternId: String,
    val organisasjonsnummer: String,
    val institusjonstype: String,
    val kategori: String,
    val startdato: LocalDate,
    val faktiskSluttdato: LocalDate,
    val kilde: String,
    val endretAv: String,
    val endringstidspunkt: LocalDateTime,
    val institusjonsnavn: String,
    val avdelingsnavn: String,
)
