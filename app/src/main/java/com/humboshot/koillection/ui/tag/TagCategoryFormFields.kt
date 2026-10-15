package com.humboshot.koillection.ui.tag

import com.humboshot.koillection.data.tag.TagCategory
import com.humboshot.koillection.data.tag.TagCategoryWrite

data class TagCategoryFormFields(
    val label: String = "",
    val description: String = "",
    /** A hex color such as `#009688`. Empty means: let the server choose. */
    val color: String = "",
) {
    /** The color in the `#rrggbb` form, or null when the field is empty. */
    private val normalizedColor: String?
        get() = color.trim().removePrefix("#").takeIf { it.isNotEmpty() }?.let { "#${it.lowercase()}" }

    fun validate(): String? = when {
        label.isBlank() -> "Label is required"
        normalizedColor != null && parseHexColor(normalizedColor) == null -> "Color must look like #009688"
        else -> null
    }

    fun toCreate(): TagCategoryWrite = TagCategoryWrite(
        label = label.trim(),
        description = description.trim().ifEmpty { null },
        color = normalizedColor,
    )

    /** Gives only the fields that differ from [original], or null when nothing changed. */
    fun toPatch(original: TagCategory): TagCategoryWrite? {
        val patch = TagCategoryWrite(
            label = label.trim().takeIf { it != original.label },
            description = description.trim().takeIf { it != original.description.orEmpty() },
            color = normalizedColor?.takeIf { !it.equals(original.color, ignoreCase = true) },
        )
        return patch.takeIf { it != TagCategoryWrite() }
    }

    companion object {
        fun from(category: TagCategory) = TagCategoryFormFields(
            label = category.label,
            description = category.description.orEmpty(),
            color = category.color.orEmpty(),
        )
    }
}
