package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.datum.DatumWrite
import com.humboshot.koillection.extension.toIri

/** Datum types the app can create and edit. Other types stay read-only. */
val EDITABLE_DATUM_TYPES = listOf("text", "number")

data class DatumFormFields(
    val label: String = "",
    val type: String = "text",
    val value: String = "",
) {
    fun validate(): String? = when {
        label.isBlank() -> "Label is required"
        type == "number" && value.isNotBlank() && value.trim().toBigDecimalOrNull() == null ->
            "Value must be a number"
        else -> null
    }

    fun toCreate(itemId: String, position: Int): DatumWrite = DatumWrite(
        item = itemId.toIri("items"),
        type = type,
        label = label.trim(),
        value = value.trim(),
        position = position,
    )

    /** Gives only the fields that differ from [original], or null when nothing changed. */
    fun toPatch(original: Datum): DatumWrite? {
        val patch = DatumWrite(
            label = label.trim().takeIf { it != original.label },
            value = value.trim().takeIf { it != original.value.orEmpty() },
        )
        return patch.takeIf { it != DatumWrite() }
    }

    companion object {
        fun from(datum: Datum) = DatumFormFields(
            label = datum.label,
            type = datum.type,
            value = datum.value.orEmpty(),
        )
    }
}
