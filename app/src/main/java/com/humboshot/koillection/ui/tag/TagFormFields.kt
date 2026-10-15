package com.humboshot.koillection.ui.tag

import com.humboshot.koillection.data.tag.Tag
import com.humboshot.koillection.data.tag.TagWrite
import com.humboshot.koillection.extension.iriId
import com.humboshot.koillection.extension.toIri

data class TagFormFields(
    val label: String = "",
    val description: String = "",
    /** The tag category id, or null for no category. */
    val categoryId: String? = null,
    val visibility: String = "public",
) {
    fun validate(): String? = if (label.isBlank()) "Label is required" else null

    fun toCreate(): TagWrite = TagWrite(
        label = label.trim(),
        description = description.trim().ifEmpty { null },
        category = categoryId?.toIri("tag_categories"),
        visibility = visibility,
    )

    /**
     * Gives only the fields that differ from [original], or null when nothing changed.
     * A cleared description is sent as "". A removed category cannot be sent, because null fields are left out.
     */
    fun toPatch(original: Tag): TagWrite? {
        val patch = TagWrite(
            label = label.trim().takeIf { it != original.label },
            description = description.trim().takeIf { it != original.description.orEmpty() },
            category = categoryId?.takeIf { it != original.category?.iriId() }?.toIri("tag_categories"),
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != TagWrite() }
    }

    companion object {
        fun from(tag: Tag) = TagFormFields(
            label = tag.label,
            description = tag.description.orEmpty(),
            categoryId = tag.category?.iriId(),
            visibility = tag.visibility ?: "public",
        )
    }
}
