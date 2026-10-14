package com.humboshot.koillection.extension

import com.humboshot.koillection.UserContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

fun String.buildUrl(): String {
    val url = UserContext().baseAddress.trimEnd('/') + "/" + this.trimStart('/')
    return url
}

/** Extracts the id from an API Platform IRI such as `/api/collections/<id>`. */
fun String.iriId(): String = substringAfterLast('/')

/** Builds an API Platform IRI from an id, for example `/api/collections/<id>`. */
fun String.toIri(resource: String): String = "/api/$resource/$this"

/**
 * Turns user input like `collection.example.com`, `Https://collection.example.com/` or
 * `https//collection.example.com` into a base URL Retrofit accepts (`https://collection.example.com/`).
 * Keeps an explicit `http://`, defaults to `https://`. Returns null when no valid host remains.
 */
fun String.normalizeServerUrl(): String? {
    val input = trim().replace(" ", "")
    val scheme = if (input.startsWith("http://", ignoreCase = true)) "http" else "https"
    val withoutScheme = input
        .replace(Regex("^[a-zA-Z]+:?//"), "")
        .trimEnd('/')

    if (withoutScheme.isEmpty() || withoutScheme.contains("://")) return null

    val url = "$scheme://$withoutScheme/".toHttpUrlOrNull() ?: return null
    if (!url.host.contains('.') && url.host != "localhost") return null

    return url.toString().let { if (it.endsWith("/")) it else "$it/" }
}
