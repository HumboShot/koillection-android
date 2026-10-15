package com.humboshot.koillection.data.loan

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Loan(
    @Json(name = "id")
    val id: String,
    @Json(name = "item")
    val item: String,
    @Json(name = "lentTo")
    val lentTo: String,
    @Json(name = "lentAt")
    val lentAt: String,
    @Json(name = "returnedAt")
    val returnedAt: String? = null,
) {
    val isReturned: Boolean get() = returnedAt != null
}
