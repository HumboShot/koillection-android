package com.humboshot.koillection.data.log

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** One history entry: an object was created or deleted. */
@JsonClass(generateAdapter = true)
data class Log(
    @Json(name = "id")
    val id: String,
    @Json(name = "type")
    val type: String? = null,
    @Json(name = "loggedAt")
    val loggedAt: String,
    @Json(name = "objectId")
    val objectId: String,
    @Json(name = "objectLabel")
    val objectLabel: String,
    @Json(name = "objectClass")
    val objectClass: String,
    @Json(name = "objectDeleted")
    val objectDeleted: Boolean = false,
) {
    /** "App\Entity\Item" becomes "Item". */
    val shortClass: String get() = objectClass.substringAfterLast('\\').substringAfterLast('.')
}

/** Newest first. A null [type] keeps every entry. */
fun filterLogs(logs: List<Log>, type: String?): List<Log> =
    logs.filter { type == null || it.type == type }.sortedByDescending { it.loggedAt }
