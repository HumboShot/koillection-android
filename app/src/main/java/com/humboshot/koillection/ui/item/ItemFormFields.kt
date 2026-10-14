package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.item.ItemWrite
import com.humboshot.koillection.extension.toIri

data class ItemFormFields(
    val name: String = "",
    val quantity: String = "1",
    val visibility: String = "public",
) {
    fun validate(): String? = when {
        name.isBlank() -> "Name is required"
        (quantity.toIntOrNull() ?: 0) < 1 -> "Quantity must be a whole number of 1 or more"
        else -> null
    }

    fun toCreate(collectionId: String): ItemWrite = ItemWrite(
        name = name.trim(),
        quantity = quantity.toInt(),
        collection = collectionId.toIri("collections"),
        visibility = visibility,
    )
}
