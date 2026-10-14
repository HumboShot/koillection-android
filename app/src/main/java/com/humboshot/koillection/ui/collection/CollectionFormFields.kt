package com.humboshot.koillection.ui.collection

import com.humboshot.koillection.data.collection.CollectionWrite
import com.humboshot.koillection.data.collection.KCollection
import com.humboshot.koillection.extension.toIri

data class CollectionFormFields(
    val title: String = "",
    val visibility: String = "public",
) {
    fun validate(): String? = if (title.isBlank()) "Title is required" else null

    fun toCreate(parentId: String?): CollectionWrite = CollectionWrite(
        title = title.trim(),
        parent = parentId?.toIri("collections"),
        visibility = visibility,
    )

    /** Gives only the fields that differ from [original], or null when nothing changed. */
    fun toPatch(original: KCollection): CollectionWrite? {
        val patch = CollectionWrite(
            title = title.trim().takeIf { it != original.title },
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != CollectionWrite() }
    }

    companion object {
        fun from(collection: KCollection) = CollectionFormFields(
            title = collection.title,
            visibility = collection.visibility,
        )
    }
}
