package com.humboshot.koillection.ui.album

import com.humboshot.koillection.data.album.Photo
import com.humboshot.koillection.data.album.PhotoWrite
import com.humboshot.koillection.extension.toIri

data class PhotoFormFields(
    val title: String = "",
    val comment: String = "",
    val place: String = "",
    val visibility: String = "public",
) {
    fun validate(): String? = if (title.isBlank()) "Title is required" else null

    fun toCreate(albumId: String): PhotoWrite = PhotoWrite(
        title = title.trim(),
        comment = comment.trim().ifEmpty { null },
        place = place.trim().ifEmpty { null },
        album = albumId.toIri("albums"),
        visibility = visibility,
    )

    /** Gives only the fields that differ from [original], or null when nothing changed. A cleared text is sent as "". */
    fun toPatch(original: Photo): PhotoWrite? {
        val patch = PhotoWrite(
            title = title.trim().takeIf { it != original.title },
            comment = comment.trim().takeIf { it != original.comment.orEmpty() },
            place = place.trim().takeIf { it != original.place.orEmpty() },
            visibility = visibility.takeIf { it != original.visibility },
        )
        return patch.takeIf { it != PhotoWrite() }
    }

    companion object {
        fun from(photo: Photo) = PhotoFormFields(
            title = photo.title,
            comment = photo.comment.orEmpty(),
            place = photo.place.orEmpty(),
            visibility = photo.visibility ?: "public",
        )
    }
}
