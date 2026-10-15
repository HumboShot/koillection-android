package com.humboshot.koillection.ui.item

import com.humboshot.koillection.data.datum.Datum
import com.humboshot.koillection.data.datum.DatumWrite

/** One text or number field in the item form. [original] is null for a field that is not on the server yet. */
data class DatumDraft(
    val key: Int,
    val original: Datum?,
    val fields: DatumFormFields,
) {
    /** A datum to show the draft with [DatumRow]. */
    fun preview(): Datum = Datum(
        id = original?.id.orEmpty(),
        type = fields.type,
        label = fields.label.trim(),
        value = fields.value.trim(),
        position = original?.position,
        currency = null,
        image = null,
        imageSmallThumbnail = null,
        imageLargeThumbnail = null,
        file = null,
        video = null,
        originalFilename = null,
    )
}

/** The server requests that make the fields of an item match the drafts. */
data class DatumChanges(
    val deletes: List<Datum>,
    val patches: List<Pair<Datum, DatumWrite>>,
    val creates: List<DatumDraft>,
) {
    val isEmpty: Boolean get() = deletes.isEmpty() && patches.isEmpty() && creates.isEmpty()
}

/** Compares [drafts] with the data on the server. Only editable types are deleted when they have no draft. */
fun datumChanges(originals: List<Datum>, drafts: List<DatumDraft>): DatumChanges {
    val keptIds = drafts.mapNotNull { it.original?.id }.toSet()
    return DatumChanges(
        deletes = originals.filter { it.type in EDITABLE_DATUM_TYPES && it.id !in keptIds },
        patches = drafts.mapNotNull { draft ->
            val original = draft.original ?: return@mapNotNull null
            draft.fields.toPatch(original)?.let { original to it }
        },
        creates = drafts.filter { it.original == null },
    )
}

fun List<Datum>.nextPosition(): Int = (maxOfOrNull { it.position ?: 0 } ?: 0) + 1
