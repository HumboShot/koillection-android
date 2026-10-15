package com.humboshot.koillection.data.template

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Template(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
)

@JsonClass(generateAdapter = true)
data class TemplateWrite(
    @Json(name = "name")
    val name: String? = null,
)

@JsonClass(generateAdapter = true)
data class Field(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "position")
    val position: Int,
    @Json(name = "type")
    val type: String,
    @Json(name = "choiceList")
    val choiceList: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)

@JsonClass(generateAdapter = true)
data class FieldWrite(
    @Json(name = "name")
    val name: String? = null,
    @Json(name = "position")
    val position: Int? = null,
    @Json(name = "type")
    val type: String? = null,
    @Json(name = "choiceList")
    val choiceList: String? = null,
    @Json(name = "template")
    val template: String? = null,
    @Json(name = "visibility")
    val visibility: String? = null,
)

/** Field types the API accepts, in the order of the web form. */
val FIELD_TYPES = listOf(
    "text", "textarea", "country", "date", "rating", "number", "price", "link", "list",
    "choice-list", "checkbox", "image", "file", "sign", "video", "blank-line", "section",
)

/** "choice-list" becomes "Choice list". */
fun fieldTypeLabel(type: String): String = type.replace('-', ' ').replaceFirstChar { it.uppercase() }
