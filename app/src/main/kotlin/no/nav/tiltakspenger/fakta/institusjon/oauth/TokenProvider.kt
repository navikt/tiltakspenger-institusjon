package no.nav.tiltakspenger.fakta.institusjon.oauth

fun interface TokenProvider {

    suspend fun getToken(): String
}
