package com.humboshot.koillection.ui.template

import com.humboshot.koillection.data.template.Field
import com.humboshot.koillection.data.template.FieldWrite
import com.humboshot.koillection.extension.iriId
import com.humboshot.koillection.extension.toIri

data class FieldFormFields(
    val name: String = "",
    val type: String = "text",
    val choiceListId: String? = null,
    val visibility: String = "public",
) {
    fun validate(): String? = when {
        name.isBlank() -> "Name is required"
        type == "choice-list" && choiceListId == null -> "Pick a choice list"
        else -> null
    }

    private val choiceListIri: String? get() = choiceListId?.takeIf { type == "choice-list" }?.toIri("choice_lists")

    fun toCreate(templateId: String, position: Int): FieldWrite = FieldWrite(
        name = name.trim(),
        position = position,
        type = type,
        choiceList = choiceListIri,
        template = templateId.toIri("templates"),
        visibility = visibility,
    )

    /**
     * Gives only the fields that differ from [original], or null when nothing changed.
     * A null `choiceList` is left out of the JSON, so a field cannot lose its choice list through this patch.
     */
    fun toPatch(original: Field): FieldWrite? {
        val patch = FieldWrite(
            name = name.trim().takeIf { it != original.name },
            type = type.takeIf { it != original.type },
            choiceList = choiceListIri?.takeIf { it.iriId() != original.choiceList?.iriId() },
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != FieldWrite() }
    }

    companion object {
        fun from(field: Field) = FieldFormFields(
            name = field.name,
            type = field.type,
            choiceListId = field.choiceList?.iriId(),
            visibility = field.visibility ?: "public",
        )
    }
}

/**
 * The position changes that move the field at [index] one place up ([delta] = -1) or down ([delta] = 1).
 * The two fields swap positions. Returns an empty list at the ends.
 */
fun swapPositions(fields: List<Field>, index: Int, delta: Int): List<Pair<String, Int>> {
    val other = fields.getOrNull(index + delta) ?: return emptyList()
    val field = fields.getOrNull(index) ?: return emptyList()
    // Equal positions would not change the order, so fall back to the list indices.
    val (fieldPosition, otherPosition) =
        if (field.position != other.position) field.position to other.position else index to index + delta
    return listOf(field.id to otherPosition, other.id to fieldPosition)
}
