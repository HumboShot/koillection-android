package com.humboshot.koillection.data.inventory

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** [content] is free JSON. The spec only says "array", so [parseInventory] reads it with care. */
@JsonClass(generateAdapter = true)
data class Inventory(
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "content")
    val content: List<Any?> = emptyList(),
    @Json(name = "createdAt")
    val createdAt: String? = null,
)

data class InventoryItem(val name: String, val checked: Boolean)

/** A collection in the inventory, with its items and its sub-collections. */
data class InventorySection(
    val title: String,
    val items: List<InventoryItem>,
    val children: List<InventorySection>,
) {
    val totalItems: Int get() = items.size + children.sumOf { it.totalItems }
    val checkedItems: Int get() = items.count { it.checked } + children.sumOf { it.checkedItems }
}

/**
 * Reads the inventory content. An object gives a section: its title is `title` or `name`, its items are
 * `items`, and its sub-sections are `children`. A plain string gives a section with only a title.
 * Unknown values are skipped, so a new server format shows less, but does not crash.
 */
fun parseInventory(content: List<Any?>): List<InventorySection> = content.mapNotNull(::parseSection)

private fun parseSection(value: Any?): InventorySection? = when (value) {
    is String -> InventorySection(title = value, items = emptyList(), children = emptyList())
    is Map<*, *> -> InventorySection(
        title = (value["title"] ?: value["name"])?.toString().orEmpty(),
        items = (value["items"] as? List<*>).orEmpty().mapNotNull(::parseItem),
        children = (value["children"] as? List<*>).orEmpty().mapNotNull(::parseSection),
    )
    else -> null
}

private fun parseItem(value: Any?): InventoryItem? = when (value) {
    is String -> InventoryItem(name = value, checked = false)
    is Map<*, *> -> InventoryItem(
        name = (value["name"] ?: value["title"])?.toString().orEmpty(),
        checked = value["checked"] == true || value["checked"] == 1.0 || value["checked"] == "1",
    )
    else -> null
}
