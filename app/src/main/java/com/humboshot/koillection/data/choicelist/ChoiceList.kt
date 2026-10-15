package com.humboshot.koillection.data.choicelist

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ChoiceList(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "choices")
    val choices: List<String?> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class ChoiceListWrite(
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "choices")
    val choices: List<String>? = null,
)
