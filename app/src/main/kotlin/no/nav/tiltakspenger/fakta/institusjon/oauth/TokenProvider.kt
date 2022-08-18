package no.nav.tiltakspenger.fakta.institusjon.oauth

fun interface TokenProvider {

    fun getToken(): String
}
