package com.humboshot.koillection.auth

/** The signed-in user, as the HTTP layer sees it. */
interface Session {
    /** Base URL of the Koillection server, for example `https://collection.example.com/`. */
    val serverUrl: String?
    val username: String?
    val password: String?
    val token: String?

    fun updateToken(token: String)

    fun signOut()
}
