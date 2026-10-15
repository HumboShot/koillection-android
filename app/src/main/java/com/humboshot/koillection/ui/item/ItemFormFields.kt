package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.item.Item
import com.humboshot.koillection.data.item.ItemWrite
import com.humboshot.koillection.extension.toIri

data class ItemFormFields(
    val name: String = "",
    val quantity: String = "1",
    val visibility: String = "public",
    /** The ids of the tags on the item, in the order the user added them. */
    val tagIds: List<String> = emptyList(),
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
        tags = tagIds.takeIf { it.isNotEmpty() }?.map { it.toIri("tags") },
    )

    /**
     * Gives only the fields that differ from [original], or null when nothing changed.
     * Tags are sent as the full list, and only when the set of tags changed.
     */
    fun toPatch(original: Item, originalTagIds: List<String> = emptyList()): ItemWrite? {
        val patch = ItemWrite(
            name = name.trim().takeIf { it != original.name },
            quantity = quantity.toInt().takeIf { it != original.quantity },
            visibility = visibility.takeIf { it != original.visibility },
            tags = tagIds.takeIf { it.toSet() != originalTagIds.toSet() }?.map { it.toIri("tags") },
        )
        return patch.takeIf { it != ItemWrite() }
    }

    fun addTag(tagId: String): ItemFormFields = if (tagId in tagIds) this else copy(tagIds = tagIds + tagId)

    fun removeTag(tagId: String): ItemFormFields = copy(tagIds = tagIds - tagId)

    companion object {
        fun from(item: Item, tagIds: List<String> = emptyList()) = ItemFormFields(
            name = item.name,
            quantity = item.quantity.toString(),
            visibility = item.visibility,
            tagIds = tagIds,
        )
    }
}
