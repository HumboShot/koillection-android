package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.item.Item
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

    /** Gives only the fields that differ from [original], or null when nothing changed. */
    fun toPatch(original: Item): ItemWrite? {
        val patch = ItemWrite(
            name = name.trim().takeIf { it != original.name },
            quantity = quantity.toInt().takeIf { it != original.quantity },
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != ItemWrite() }
    }

    companion object {
        fun from(item: Item) = ItemFormFields(
            name = item.name,
            quantity = item.quantity.toString(),
            visibility = item.visibility,
        )
    }
}
